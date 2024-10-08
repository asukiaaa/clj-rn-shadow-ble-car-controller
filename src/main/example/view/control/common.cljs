(ns example.view.control.common
  (:require [reagent.core :as r]
            ["react-native" :as rn]))

(defn- limit-range [value]
  (-> value
      (min 1)
      (max -1)))

(defn- rate-x-y [p-x p-y view-x view-y view-w view-h]
  (let [harf-w (/ view-w 2)
        harf-h (/ view-h 2)
        rate-x (/ (- p-x view-x harf-w) harf-w)
        rate-y (/ (- p-y view-y harf-h) harf-h)]
    {:x (limit-range rate-x)
     :y (limit-range rate-y)}))

(defn joystick [id on-move on-release & [{:keys [width height] :or {width 300 height 300}}]]
  (let [view-x (r/atom nil)
        view-y (r/atom nil)
        view-w width
        view-h height
        center-r 75
        target-r 50
        posi-x-base (/ view-w 2)
        posi-y-base (/ view-h 2)
        value-x (r/atom 0)
        value-y (r/atom 0)
        ref (str "joystick-" id)
        ref-obj (r/atom nil)
        update-view-x-y #(.measure @ref-obj (fn [fx fy w h px py]
                                              (reset! view-x px)
                                              (reset! view-y py)))
        on-move (fn [{:keys [x y] :as xy}]
                  (reset! value-x x)
                  (reset! value-y y)
                  (on-move xy))
        on-release (fn []
                     (reset! value-x 0)
                     (reset! value-y 0)
                     (on-release))
        action (fn [^js evt]
                 (-> (rate-x-y (.-pageX (.-nativeEvent evt))
                               (.-pageY (.-nativeEvent evt))
                               @view-x @view-y view-w view-h)
                     (on-move)))]
    (r/create-class
     {:reagent-render
      (fn []
        [:> rn/View
         {:ref ref
          :style {:background-color "#beb"
                  :align-self "center"
                  :width view-w
                  :height view-h}
          :on-layout #(update-view-x-y)
          :on-start-should-set-responder (fn [] true)
          :on-move-should-set-responder (fn [] true)
          :on-responder-grant #(action %)
          :on-responder-move #(action %)
          :on-responder-release #(on-release)}
         [:> rn/View {:style {:background-color "#fff"
                              :width (* center-r 2)
                              :height (* center-r 2)
                              :border-radius center-r
                              :position "absolute"
                              :top (- (/ view-h 2) center-r)
                              :left (- (/ view-w 2) center-r)}}]
         [:> rn/View {:style {:background-color "#777"
                              :width (* target-r 2)
                              :height (* target-r 2)
                              :border-radius target-r
                              :position "absolute"
                              :top (- (+ posi-y-base (* posi-y-base @value-y)) target-r)
                              :left (- (+ posi-x-base (* posi-x-base @value-x)) target-r)}}]])

      :component-did-mount
      #(reset! ref-obj (-> (.-refs ^js %)
                           (aget ref)))})))
