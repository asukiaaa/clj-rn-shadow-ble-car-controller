(ns example.view.control.single-joystick
  (:require [reagent.core :as r]
            [example.view.control.common :as control.common]
            [example.events.ble :as event-ble]
            [re-frame.core :refer [dispatch]]))

(defn rate->byte [rate]
  (-> rate
      (min 1)
      (max 0)
      (* 255)))

(defn f+b->byte [f b]
  (int (/ (+ f (- 255 b)) 2)))

(defn set-speed [{:keys [x y]}]
  (when (and x y)
    (let [f (rate->byte (- y))
          b (rate->byte y)
          l (rate->byte (- x))
          r (rate->byte x)
          lf (max 0 (- f l))
          rf (max 0 (- f r))
          lb (max 0 (- b l))
          rb (max 0 (- b r))
          speed {:l (f+b->byte lf lb) :r (f+b->byte rf rb)}]
      (dispatch [:set-speed speed]))))

(defn core []
  (r/with-let [interval (r/atom nil)
               set-interval (fn []
                              (reset! interval (js/setInterval event-ble/send-speed 50)))
               clear-interval #(js/clearInterval @interval)]
    (r/create-class
     {:reagent-render
      (fn []
        [control.common/joystick :single-joystick #(set-speed %) #(set-speed {:x 0 :y 0})])
      :component-did-mount set-interval
      :component-will-unmount clear-interval})))
