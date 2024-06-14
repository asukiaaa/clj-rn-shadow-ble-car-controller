(ns example.subs
  (:require [re-frame.core :as rf :refer [reg-sub]]))

(rf/reg-sub
 :get-counter
 (fn [db _]
   (:counter db)))

(rf/reg-sub
 :counter-tappable?
 (fn [db _]
   (:counter-tappable? db)))

(rf/reg-sub
 :navigation/root-state
 (fn [db _]
   (get-in db [:navigation :root-state])))

(reg-sub
 :get-devices
 (fn [db _]
   (:devices db)))

(reg-sub
 :get-current-device
 (fn [db _]
   (:current-device db)))

(reg-sub
 :get-page
 (fn [db _]
   (:page db)))

(reg-sub
 :speed
 (fn [db _]
   (:speed db)))

(reg-sub
 :sent-speed
 (fn [db _]
   (:sent-speed db)))

(reg-sub
 :mag-values
 (fn [db _]
   (:mag-values db)))

(reg-sub
 :direction-speed
 (fn [db _]
   (:direction-speed db)))

(reg-sub
 :sent-direction-speed
 (fn [db _]
   (:sent-direction-speed db)))
