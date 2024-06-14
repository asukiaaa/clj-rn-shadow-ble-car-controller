(ns example.app
  (:require [example.events]
            [example.subs]
            [example.widgets :refer [button]]
            [expo.root :as expo-root]
            ["expo-status-bar" :refer [StatusBar]]
            [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
            ["react-native" :as rn]
            [reagent.core :as r]
            ["@react-navigation/native" :as rnn]
            ["@react-navigation/native-stack" :as rnn-stack]
            [example.devices.ble :as ble]))

(defonce shadow-splash (js/require "../assets/shadow-cljs.png"))
(defonce cljs-splash (js/require "../assets/cljs.png"))

(defonce Stack (rnn-stack/createNativeStackNavigator))

(defn device-button [device]
  [:> rn/TouchableHighlight
   {:style {:background-color "#4a4" :border-radius 5 :padding 10 :margin 5}
    :on-press (fn []
                (.log js/console "todo")
                (dispatch [:set-page :ble-control])
                (dispatch [:set-current-device device]))}
   [:> rn/Text {:style {:color "#fff"}}
    [:> rn/Text (:name device) " " (:id device)]]])

(defn devices-box []
  (let [devices (subscribe [:get-devices])]
    (fn []
      [:> rn/View
       (if (empty? @devices)
         [:> rn/Text {:style {:margin 10}}
          "no device"]
         [:> rn/FlatList
          {:data (clj->js @devices)
           :key-extractor (fn [item index]
                            (:id (js->clj item :keywordize-keys true)))
           :render-item #(let [device (:item (js->clj % :keywordize-keys true))]
                           (r/as-element [device-button device]))}])])))

(defn home [^js props]
  (r/with-let [counter (rf/subscribe [:get-counter])
               tap-enabled? (rf/subscribe [:counter-tappable?])]
    [:> rn/View {:style {:flex 1
                         :padding-vertical 50
                         :justify-content :space-between
                         :align-items :center
                         :background-color :white}}
     [:> rn/TouchableOpacity {:style {:background-color :green :padding 10}
                              :on-press (fn []
                                          (ble/init)
                                          #_(.alert rn/Alert "call init"))}
      [:> rn/Text "init"]]
     [button {:on-press #(ble/scan)}
      "scan"]
     [devices-box]
     [:> rn/View {:style {:align-items :center}}
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     72
                           :color         :blue
                           :margin-bottom 20}} @counter]
      [button {:on-press #(rf/dispatch [:inc-counter])
               :disabled? (not @tap-enabled?)
               :style {:background-color :blue}}
       "Tap me, I'll count"]]
     [:> rn/View {:style {:align-items :center}}
      [button {:on-press (fn []
                           (-> props .-navigation (.navigate "About")))}
       "Tap me, I'll navigate"]]
     [:> rn/View
      [:> rn/View {:style {:flex-direction :row
                           :align-items :center
                           :margin-bottom 20}}
       [:> rn/Image {:style {:width  160
                             :height 160}
                     :source cljs-splash}]
       [:> rn/Image {:style {:width  160
                             :height 160}
                     :source shadow-splash}]]
      [:> rn/Text {:style {:font-weight :normal
                           :font-size   15
                           :color       :blue}}
       "Using: shadow-cljs+expo+reagent+re-frame"]]
     [:> StatusBar {:style "auto"}]]))

(defn- about
  []
  (r/with-let [counter (rf/subscribe [:get-counter])]
    [:> rn/View {:style {:flex 1
                         :padding-vertical 50
                         :padding-horizontal 20
                         :justify-content :space-between
                         :align-items :flex-start
                         :background-color :white}}
     [:> rn/View {:style {:align-items :flex-start}}
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     54
                           :color         :blue
                           :margin-bottom 20}}
       "About Example App"]
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     20
                           :color         :blue
                           :margin-bottom 20}}
       (str "Counter is at: " @counter)]
      [:> rn/Text {:style {:font-weight :normal
                           :font-size   15
                           :color       :blue}}
       "Built with React Native, Expo, Reagent, re-frame, and React Navigation"]]
     [:> StatusBar {:style "auto"}]]))

(defn root []
  ;; The save and restore of the navigation root state is for development time bliss
  (r/with-let [!root-state (rf/subscribe [:navigation/root-state])
               save-root-state! (fn [^js state]
                                  (rf/dispatch [:navigation/set-root-state state]))
               add-listener! (fn [^js navigation-ref]
                               (when navigation-ref
                                 (.addListener navigation-ref "state" save-root-state!)))]
    [:> rnn/NavigationContainer {:ref add-listener!
                                 :initialState (when @!root-state (-> @!root-state .-data .-state))}
     [:> Stack.Navigator
      [:> Stack.Screen {:name "Home"
                        :component (fn [props] (r/as-element [home props]))
                        :options {:title "Example App"}}]
      [:> Stack.Screen {:name "About"
                        :component (fn [props] (r/as-element [about props]))
                        :options {:title "About"}}]]]))

(defn start
  {:dev/after-load true}
  []
  (expo-root/render-root (r/as-element [root])))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (start))