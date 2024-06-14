(ns example.view.control.index
  (:require [clojure.string :as str]
            [reagent.core :as r]
            ["react-native" :as rn]
            [example.devices.ble :as ble]
            #_[example.views.common :as v.common]
            #_[example.views.ble-control.common :as v.ble-common]
            [example.view.control.single-joystick :as v.single-joystick]
            [example.view.control.tile-buttons :as v.tile-buttons]
            [example.view.control.toggle-bars :as v.toggle-bars]
            #_[example.views.ble-control.tile-buttons :as v.tile-buttons]
            #_[example.views.ble-control.toggle-bars :as v.toggle-bars]
            #_[example.views.ble-control.magnetometer :as v.mag]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(def control-modes
  [{:id :single-joystick
    :name "Joystick"}
   {:id :toggle-bars
    :name "Bars"}
   {:id :tile-buttons
    :name "Buttons"}
   #_{:id :magnet
      :name "Magnet"}])

(defn control-area []
  (r/with-let [control-mode (r/atom (:id (first control-modes)))
               current-device (subscribe [:get-current-device])]
    [:> rn/View
     [:> rn/View {:style {:flex-direction "row"
                          :margin-bottom 20
                          :align-self "center"}}
      (doall
       (for [{:keys [id name]} control-modes
             :let [selected? (= @control-mode id)]]
         ^{:key (str :mode-select- id)}
         [:> rn/TouchableHighlight
          {:style {:background-color "#ccc"
                   :padding 10
                   :border-width 3
                   :border-color (if selected? "#666" "#ccc")
                   :margin 5
                   :border-radius 5}
           :on-press #(reset! control-mode id)}
          [:> rn/Text name]]))]
     (case @control-mode
       :tile-buttons [v.tile-buttons/core]
       :toggle-bars [v.toggle-bars/core]
           ; :magnet [v.mag/mag-panel]
       [v.single-joystick/core])]))

(defn core [props]
  #_(.log js/console props)
  (r/with-let [current-device (subscribe [:get-current-device])
               connected? (r/atom false)]
    (r/create-class
     {:reagent-render
      (fn []
        [:> rn/View
         [:> rn/View {:style {:flex-direction "column" :align-items "flex-start" :margin 10}}
          [:> rn/Text {:style {:font-size 20 :font-weight "100" :width "100%"}}
           (:name @current-device)]
          [:> rn/Text (:id @current-device)]]
         (if @connected?
           [control-area]
           [:> rn/View {:style {:align-content "center" :align-self "center"}}
            [:> rn/Text "connecting"]])])
      :UNSAFE-component-will-mount (fn []
                                     (.log js/console "connect to device")
                                     (ble/connect (:id @current-device)
                                                  :on-success (fn []
                                                                (reset! connected? true))))
      :UNSAFE-component-will-unmount (fn []
                                       (.log js/console "disconnect from device")
                                       (ble/disconnect (:id @current-device)))})))
