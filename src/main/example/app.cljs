(ns example.app
  (:require [example.events]
            [example.subs]
            [expo.root :as expo-root]
            [re-frame.core :as rf]
            [reagent.core :as r]
            ["@react-navigation/native" :as rnn]
            ["@react-navigation/native-stack" :as rnn-stack]
            [example.view.control.index :as view.control]
            [example.view.info :as view.info]
            [example.view.home :as view.home]
            [example.devices.ble :as ble]))

(defonce Stack (rnn-stack/createNativeStackNavigator))

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
                        :component (fn [props] (r/as-element [:f> view.home/core props]))
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
