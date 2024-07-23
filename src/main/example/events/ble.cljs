(ns example.events.ble
  (:require ["moment" :as moment]
            [example.devices.ble :as ble]
            [re-frame.core :refer [subscribe dispatch reg-event-fx]]))

(def service-id "00ff")
(def characteristic-id "ff01")

(defn- ble-send [{:keys [l r]}]
  (let [current-device (subscribe [:get-current-device])
        data (concat [(.charCodeAt "m" 0)]
                     (map #(if (nil? %) 0 %) [l r]))]
    (ble/write (:id @current-device) service-id characteristic-id data)))

(defn- ble-send-direction [{:keys [degree speed]}]
  (let [current-device (subscribe [:get-current-device])
        degree (int degree)
        plus-degree (if (> degree 0) degree 0)
        minus-degree (if (< degree 0) (- degree) 0)
        data [(.charCodeAt "d" 0) plus-degree minus-degree (int speed)]]
    (ble/write (:id @current-device) service-id characteristic-id data)))

(defn- same-speed? [speed1 speed2]
  (and (= (:l speed1) (:r speed2))
       (= (:l speed1) (:r speed2))))

(defn- stop-speed? [speed]
  (empty?
   (for [key [:l :r]
         :let [value (key speed)]
         :when (and (not= speed 128) (not= speed 127))]
     key)))

(defn send-speed []
  (let [speed (subscribe [:speed])
        sent-speed (subscribe [:sent-speed])
        now-50ms (.subtract (moment) 50 "ms")]
    #_(.log js/console "send")
    (when (or (not (same-speed? @speed @sent-speed))
              (and (not (stop-speed? @speed))
                   (.isAfter now-50ms (moment (:sent_at @sent-speed)))))
      #_(println "send" sent-speed)
      (ble-send @speed)
      (dispatch [:set-sent-speed (assoc @speed :sent_at (js/Date.))]))))

(defn send-direction []
  (let [direction-speed (subscribe [:direction-speed])
        sent-direction-speed (subscribe [:sent-direction-speed])]
    (when-not (and (= 0 (:speed @sent-direction-speed)) (= 0 (:speed @direction-speed)))
      (ble-send-direction @direction-speed)
      (dispatch [:set-sent-direction-speed @direction-speed]))))
