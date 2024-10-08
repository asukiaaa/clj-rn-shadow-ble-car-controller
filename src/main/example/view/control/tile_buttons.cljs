(ns example.view.control.tile-buttons
  (:require [reagent.core :as r]
            ["react-native" :as rn]
            [example.events.ble :as event-ble]
            [re-frame.core :refer [dispatch]]
            ["react" :as react]))

(defn- set-speed [speed]
  (dispatch [:set-speed (merge {:l 128 :r 128} speed)]))

(defn- control-button [label speed]
  (let [set-and-send-speed (fn [speed]
                             (set-speed speed)
                             (event-ble/send-speed))]
    [:> rn/View
     {:style {:background-color "#494" :width 100 :height 100 :margin 5 :border-radius 5}
      :justify-content "center"
      :align-items "center"
      :on-start-should-set-responder (fn [evt] true)
      :on-responder-grant #(set-and-send-speed speed)
      :on-responder-release #(set-and-send-speed {})}
     [:> rn/Text {:style {:color "#fff" :text-align "center" :height 100}}
      label]]))

(defn core []
  (let [interval (r/atom nil)
        set-interval #(reset! interval (js/setInterval event-ble/send-speed 50))
        clear-interval #(js/clearInterval @interval)]
    (react/useEffect
     (fn []
       (set-interval)
       (fn []
         (clear-interval)))
     #js [])
    [:> rn/View {:style {:align-content "center" :align-self "center"}}
     [:> rn/View {:style {:flex-direction "row"}}
      [control-button "left foreward" {:r 255}]
      [control-button "forward"       {:l 255 :r 255}]
      [control-button "right-forward" {:l 255}]]
     [:> rn/View {:style {:flex-direction "row"}}
      [control-button "turn left"     {:l 0 :r 255}]
      [:> rn/View {:style {:width 100 :height 100 :margin 5}}]
      [control-button "trun right"    {:l 255 :r 0}]]
     [:> rn/View {:style {:flex-direction "row"}}
      [control-button "back left"     {:r 0}]
      [control-button "back"          {:l 0 :r 0}]
      [control-button "back right"    {:l 0}]]]))
