(ns pass-android.android.modals.drawer
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.utils :refer [create-element hex-alph decrypt]]
            [pass-android.android.state :refer [refs]]
            [goog.crypt.base64 :as b64]
            [rum.core :as rum]))

(def fs (js/require "react-native-fs"))
(def ReactNative (js/require "react-native"))
(def MaterialIcons (js/require "react-native-vector-icons/MaterialIcons"))

(def DrawerLayout (.-DrawerLayoutAndroid ReactNative))
(def drawer-layout (partial create-element DrawerLayout))
(def icon (partial create-element (.-default MaterialIcons)))
(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def flat-list (partial create-element (.. ReactNative -FlatList)))
(def touchable-native-feedback (partial create-element (.-TouchableNativeFeedback ReactNative)))

(defc drawer-row
  [state x]
  (let [m @state
        item (js->clj (.-item x) :keywordize-keys true)
        selected-option (get m :selected-menu "pass-list")
        [bg-color fg-color] (if (= (:key item) selected-option)
                              ["#eeeeee" "#009688"]
                              ["white" "black"])
        icon-name (case (:key item)
                    "pass-list" "lock"
                    "folder")]
    (touchable-native-feedback {:onPress (fn []
                                           (swap! state #(-> %
                                                             (assoc :selected-menu (:key item))
                                                             (assoc :drawer-open? false))))}
                               (view {:flexDirection "row"
                                      :alignItems "center"
                                      :style {:padding 16
                                              :paddingLeft 16
                                              :backgroundColor bg-color}}
                                     (icon {:name icon-name
                                            :size 25
                                            :color fg-color
                                            :style {:marginRight 10}})
                                     (text {:style {:fontSize 15
                                                    :color fg-color}}
                                           (get item :text))))))

(defc drawer
  [state]
  (view
    (view {:backgroundColor "#009688"
           :paddingLeft 20
           :flexDirection "row"
           :alignItems "center"
           :height 70
           :elevation 5}
          (text {:style {:color "white"
                         :fontSize 30}}
                "Menu"))
    (flat-list {
                :data [{:text "Pass list" :key :pass-list}
                       {:text "Change path" :key :change-path}]
                :renderItem #(drawer-row state %)})))

(defc drawer-modal
  [state & children]
  (let [m @state]
    (drawer-layout {:renderNavigationView #(drawer state)
                    :drawerWidth 300
                    :ref #(swap! refs assoc :drawer %)
                    :onDrawerClose #(swap! state assoc :drawer-open? false)
                    :onDrawerOpen #(swap! state assoc :drawer-open? true)
                    :drawerPosition (.. DrawerLayout -positions -Left)}
                   children)))
