(ns example.events.ble
  (:require ["moment" :as moment]
            [example.devices.ble :as ble]
            [re-frame.core :refer [dispatch reg-event-db]]))

(def service-id "00ff")
(def characteristic-id "ff01")

(defn- ble-send [{:keys [l r]} id-device]
  (let [data (concat [(.charCodeAt "m" 0)]
                     (map #(if (nil? %) 0 %) [l r]))]
    (ble/write id-device service-id characteristic-id data)))

(defn- ble-send-direction [{:keys [degree speed]} id-device]
  (let [degree (int degree)
        plus-degree (if (> degree 0) degree 0)
        minus-degree (if (< degree 0) (- degree) 0)
        data [(.charCodeAt "d" 0) plus-degree minus-degree (int speed)]]
    (ble/write id-device service-id characteristic-id data)))

(defn- same-speed? [speed1 speed2]
  (and (= (:l speed1) (:r speed2))
       (= (:l speed1) (:r speed2))))

(defn- stop-speed? [speed]
  (empty?
   (for [key [:l :r]
         :let [value (key speed)]
         :when (and (not= speed 128) (not= speed 127))]
     key)))

(reg-event-db
 :ble-send-speed
 (fn [db _]
   (let [speed (:speed db)
         sent-speed (:sent-speed db)
         id-current-device (-> (:current-device db) :id)
         now-50ms (.subtract (moment) 50 "ms")]
     (if (or (not (same-speed? speed sent-speed))
             (and (not (stop-speed? speed))
                  (.isAfter now-50ms (moment (:sent_at sent-speed)))))
       (do
         (ble-send speed id-current-device)
         (assoc db :sent-speed (assoc speed :sent_at (js/Date.))))
       db))))

(defn send-speed []
  (dispatch [:ble-send-speed]))

(reg-event-db
 :ble-send-direction
 (fn [db _]
   (let [direction-speed (:direction-speed db)
         sent-direction-speed (:sent-direction-speed :db)
         id-current-device (-> (:current-device db) :id)]
     (if (and (= 0 (:speed sent-direction-speed)) (= 0 (:speed direction-speed)))
       db
       (do
         (ble-send-direction direction-speed id-current-device)
         (assoc db :sent-direction-speed direction-speed))))))

(defn send-direction []
  (dispatch :ble-send-direction))
