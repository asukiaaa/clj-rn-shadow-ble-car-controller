(ns example.devices.ble
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [example.events]
            [example.subs]
            ["react-native" :as ReactNative]
            ["react-native-ble-manager$default" :as rn-ble-manager]))

(defonce native-modules (.-NativeModules ReactNative))
(defonce ble-manager-module (.-rn-ble-manager native-modules))
(defonce native-event-emitter (.-NativeEventEmitter ReactNative))
(defonce ble-manager-emitter (new native-event-emitter ble-manager-module))
(defonce permissions-android (.-PermissionsAndroid ReactNative))
(defonce platform (.-Platform ReactNative))

(defn handle-discovered-peripheral [data]
  (dispatch [:add-device (js->clj data :keywordize-keys true)]))

(defn start-ble-manager []
  (-> (.start rn-ble-manager #_{:showAlert false :allowDuplication false})
      (.then (fn []
               (.enableBluetooth rn-ble-manager)
               #_(js/alert "module initialized")))
      (.catch (fn [error] (.log js/console error))))
  #_(.addListener ble-manager-emitter "BleManagerStopScan" #(js/alert "stopped scanning"))
  (.addListener ble-manager-emitter "BleManagerDiscoverPeripheral" handle-discovered-peripheral))

#_(defn check-and-request-permission [{:keys [permission on-ok on-reject]}]
    (-> (.check permissions-android permission)
        #_(.catch (on-reject "on check"))
        (.then (fn [result]
                 (println permission result)
                 (if result
                   (on-ok)
                   (-> (.request permissions-android permission)
                       #_(.catch (on-reject "catch for request"))
                       (.then (fn [result]
                                (println permission result)
                                (if result
                                  (on-ok)
                                  (on-reject))
                                #_(js/alert (str "requested " result))))))))))

#_(defn check-and-request-permissions [{:keys [permissions on-ok on-reject]}]
    (for [permission permissions]
      (check-and-request-permission
       {:permission permission
        :on-ok "todo"})))

(defn init []
  (when (= (.-OS platform) "android")
    (let [is-android12-or-more (> (.-Version platform) 30)
          permission-coarse-location (-> permissions-android
                                         .-PERMISSIONS
                                         .-ACCESS_COARSE_LOCATION)
          permission-fine-location (-> permissions-android
                                       .-PERMISSIONS
                                       .-ACCESS_FINE_LOCATION)
          permission-blueooth-connect (-> permissions-android .-PERMISSIONS .-BLUETOOTH_CONNECT)
          permission-blueooth-scan (-> permissions-android .-PERMISSIONS .-BLUETOOTH_SCAN)
          permissions-to-ask (->> [(when-not is-android12-or-more permission-coarse-location)
                                   (when-not is-android12-or-more permission-fine-location)
                                   permission-blueooth-connect permission-blueooth-scan nil]
                                  (filter #(not (nil? %)))
                                  #_(into []))]
      (-> permissions-android
          (.requestMultiple (clj->js permissions-to-ask))
          (.then (start-ble-manager)))
      #_(check-and-request-permission
         {:permission permission-coarse-location
          :on-reject (fn [error] (.log js/console "rejected" error))
          :on-ok
          (fn []
            (check-and-request-permission
             {:permission permission-fine-location
              :on-reject (fn [error] (.log js/console "rejected" error))
              :on-ok
              (fn []
                (check-and-request-permission
                 {:permission permission-blueooth-connect
                  :on-reject (fn [error] (.log js/console "rejected" error))
                  :on-ok
                  #_(fn [] (.log js/console "ready to run"))
                  (fn []
                    (check-and-request-permission
                     {:permission permission-blueooth-scan
                      :on-reject (fn [error] (.log js/console "rejected" error))
                      :on-ok
                      #_(fn [] (.log js/console "ready to run"))
                      #(start-ble-manager)}))
                  #_(fn [] (.log js/console "ready to run"))
                  #_(start-ble-manager)}))}))})
      #_(-> (.check permissions-android permission-location)
            (.then (fn [result]
                     (if result
                       (start-ble-manager)
                       (-> (.request permissions-android permission-location)
                           (.then (fn [result]
                                    (start-ble-manager)
                                    #_(js/alert (str "requested " result))))))))))))

(defn scan []
  #_(.log js/console rn-ble-manager)
  (.log js/console "start")
  (-> (.scan rn-ble-manager (clj->js []) 5 true)
      (.catch (fn [e] (.log js/console "fialed start")))
      (.then (fn [e]
               (.log js/console "started")
               #_(js/alert "scan started")))))

(defn connect [device-id & {:keys [on-success on-error]}]
  (-> (.connect rn-ble-manager device-id)
      (.then (fn []
               (when on-success (on-success))))
      (.catch (fn [error]
                (when on-error (on-error error))))))

(defn disconnect [device-id & {:keys [on-success on-error]}]
  (-> (.disconnect rn-ble-manager device-id)
      (.then (fn []
               (when on-success (on-success))))
      (.catch (fn [error]
                (when on-error (on-error error))))))

(defn write [device-id service-id chara-id data]
  (-> (.retrieveServices rn-ble-manager device-id)
      (.then (fn [peri-info]
               (.writeWithoutResponse rn-ble-manager device-id service-id chara-id
                                      (clj->js data))))
      (.catch (fn [error]
                (when (= error "Device is not connected")
                  (connect device-id
                           :on-success #(write device-id service-id chara-id data)))))))
