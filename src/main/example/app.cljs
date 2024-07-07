(ns example.app
  (:require [example.events]
            [example.subs]
            [example.widgets :refer [button]]
            [expo.root :as expo-root]
            [re-frame.core :as rf :refer [subscribe dispatch]]
            ["react-native" :as rn]
            [reagent.core :as r]
            ["@react-navigation/native" :as rnn]
            ["@react-navigation/native-stack" :as rnn-stack]
            [example.devices.ble :as ble]
            [example.view.control.index :as view.control]
            [example.view.info :as view.info]))

(defonce Stack (rnn-stack/createNativeStackNavigator))

(defn device-button [{:keys [device navigation]}]
  [:> rn/TouchableHighlight
   {:style {:background-color "#4a4" :border-radius 5 :padding 10 :margin 5}
    :on-press (fn []
                (dispatch [:set-current-device device])
                (.navigate navigation "DeviceControl"))}
   [:> rn/Text {:style {:color "#fff"}}
    [:> rn/Text (:name device) " " (:id device)]]])

(defn devices-box [{:keys [navigation]}]
  (r/with-let [devices (subscribe [:get-devices])]
    [:> rn/View {:style {:flex 1}}
     (if (empty? @devices)
       [:> rn/Text {:style {:margin 10}}
        "no device"]
       #_[:> rn/ScrollView
          (for [i (range 20)] [:> rn/Text {:key i} "hi " i])]
       [:> rn/FlatList
        {:data (clj->js (reverse @devices))
         :style {:flat 1}
         :key-extractor (fn [item _index]
                          (:id (js->clj item :keywordize-keys true)))
         :render-item #(let [device (:item (js->clj % :keywordize-keys true))]
                         (r/as-element [device-button {:device device :navigation navigation}]))}])]))

(defn home [^js props]
  (r/with-let [navigation (-> props .-navigation)]
    [:> rn/View {:style {:background-color :white :height "100%"}}
     [:> rn/View {:style {:align-items :center} :margin-top 10}
      [button {:on-press (fn []
                           (ble/init)
                           (ble/scan))}
       "scan"]]
     [:f> devices-box {:navigation navigation}]]))

(defn root []
  ;; The save and restore of the navigation root state is for development time bliss
  (r/with-let [!root-state (rf/subscribe [:navigation/root-state])
               save-root-state! (fn [^js state]
                                  (rf/dispatch [:navigation/set-root-state state]))
               add-listener! (fn [^js navigation-ref]
                               (when navigation-ref
                                 (.addListener navigation-ref "state" save-root-state!)))]
    (ble/init)
    [:> rnn/NavigationContainer {:ref add-listener!
                                 :initialState (when @!root-state (-> @!root-state .-data .-state))}
     [:> Stack.Navigator
      [:> Stack.Screen {:name "Home"
                        :component (fn [props] (r/as-element [:f> home props]))
                        :options (fn [props]
                                   (clj->js
                                    {:title "BLE car con"
                                     :headerRight (fn [] (r/as-element [:f> view.info/button-top props]))}))}]
      [:> Stack.Screen {:name "DeviceControl"
                        :component (fn [props] (r/as-element [:f> view.control/core props]))}]
      [:> Stack.Screen {:name "Info"
                        :component (fn [props] (r/as-element [:f> view.info/core props]))}]]]))

(defn start
  {:dev/after-load true}
  []
  (expo-root/render-root (r/as-element [root])))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (start))
