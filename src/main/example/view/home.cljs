(ns example.view.home
  (:require ["react-native" :as rn]
            [reagent.core :as r]
            [re-frame.core :as rf :refer [subscribe dispatch]]
            [example.devices.ble :as ble]
            [example.widgets :refer [button]]))

(defn device-button [{:keys [device navigation]}]
  [:> rn/TouchableHighlight
   {:style {:background-color "#4a4" :border-radius 5 :padding 10 :margin 5}
    :on-press (fn []
                (dispatch [:set-current-device device])
                (.navigate navigation "DeviceControl"))}
   [:> rn/Text {:style {:color "#fff"}}
    [:> rn/Text (:name device) " " (:id device)]]])

(defn devices-box [{:keys [navigation]}]
  (r/with-let [devices (subscribe [:get-devices])]
    [:> rn/View {:style {:flex 1}}
     (if (empty? @devices)
       [:> rn/Text {:style {:margin 10}}
        "no device"]
       #_[:> rn/ScrollView
          (for [i (range 20)] [:> rn/Text {:key i} "hi " i])]
       [:> rn/FlatList
        {:data (clj->js (reverse @devices))
         :style {:flat 1}
         :key-extractor (fn [item _index]
                          (:id (js->clj item :keywordize-keys true)))
         :render-item #(let [device (:item (js->clj % :keywordize-keys true))]
                         (r/as-element [device-button {:device device :navigation navigation}]))}])]))

(defn core [^js props]
  (r/with-let [navigation (-> props .-navigation)]
    [:> rn/View {:style {:background-color :white :height "100%"}}
     [:> rn/View {:style {:align-items :center} :margin-top 10}
      [button {:on-press (fn []
                           (ble/init)
                           (ble/scan))}
       "scan"]]
     [:f> devices-box {:navigation navigation}]]))
