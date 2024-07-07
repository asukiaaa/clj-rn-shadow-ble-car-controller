(ns example.view.info
  (:require ["react-native" :as rn]
            ["react-native-device-info" :as device-info]
            #_["@expo/vector-icons/Feather$default" :as icon-feather]
            ["@expo/vector-icons/Ionicons$default" :as icon-ionicons]))

(defn button-top [^js props]
  (let [navigation (-> props .-navigation)]
    [:> rn/TouchableHighlight {:on-press #(.navigate navigation "Info")
                               :underlay-color :gainsboro
                               :style {:border-radius 10}}
     #_[:> icon-feather {:name "menu" :size 25}]
     [:> icon-ionicons {:name "information-circle-outline" :size 30}]]))

(defn core [^js props]
  [:> rn/View {:style {:flex 1 :background-color :white}}
   [:> rn/Text {:style {:padding 5 :font-size 20}} "version " (.getVersion device-info)]])
