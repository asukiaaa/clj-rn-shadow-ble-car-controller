(ns example.events.core
  (:require
   [re-frame.core :as rf :refer [reg-event-db]]
   [example.db :as db :refer [app-db]]))

(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   app-db))

(rf/reg-event-db
 :inc-counter
 (fn [db [_ _]]
   (update db :counter inc)))

(rf/reg-event-db
 :navigation/set-root-state
 (fn [db [_ navigation-root-state]]
   (assoc-in db [:navigation :root-state] navigation-root-state)))

(reg-event-db
 :add-device
 (fn [db [_ device]]
   ; avoid duplicate saving
   ; may be bug of ble scanning
   (let [device-id (:id device)
         device-ids (map :id (:devices db))]
     (if (some #(= % device-id) device-ids)
       db
       (update db :devices #(conj % device))))))

(reg-event-db
 :set-devices
 (fn [db [_ devices]]
   (assoc db :devices devices)))

(reg-event-db
 :set-current-device
 (fn [db [_ device]]
   (assoc db :current-device device)))

(reg-event-db
 :set-page
 (fn [db [_ page]]
   (assoc db :page page)))

(reg-event-db
 :set-speed
 (fn [db [_ speed]]
   (assoc db :speed speed)))

(reg-event-db
 :set-sent-speed
 (fn [db [_ speed]]
   (assoc db :sent-speed speed)))

(reg-event-db
 :set-mag-values
 (fn [db [_ mag-values]]
   (assoc db :mag-values mag-values)))

(reg-event-db
 :set-directoin-speed
 (fn [db [_ direction-speed]]
   (assoc db :direction-speed direction-speed)))

(reg-event-db
 :set-sent-direction-speed
 (fn [db [_ direction-speed]]
   (assoc db :sent-direction-speed direction-speed)))
