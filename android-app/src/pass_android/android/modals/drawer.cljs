(ns pass-android.android.modals.drawer
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.utils :refer [create-element hex-alph decrypt]]
            [pass-android.android.state :refer [refs]]
            [pass-android.android.actions :refer [dispatch!]]
            [clojure.string :refer [join]]
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

(defc drawer-row-pass-list
  [state]
  (let [m @state
        selected-option (get m :selected-menu :pass-list)
        [bg-color fg-color] (if (= :pass-list selected-option)
                              ["#eeeeee" "#009688"]
                              ["white" "black"])]
    (touchable-native-feedback {:onPress (fn []
                                           (if (= selected-option :pass-list)
                                             (swap! state assoc :drawer-open? false)
                                             (swap! state #(-> %
                                                               (assoc :selected-menu :pass-list)
                                                               (assoc :folder-loading? true)
                                                               (assoc :drawer-open? false)))))}
                               (view {:flexDirection "row"
                                      :alignItems "center"
                                      :style {:padding 16
                                              :paddingLeft 16
                                              :backgroundColor bg-color}}
                                     (icon {:name "lock"
                                            :size 25
                                            :color fg-color
                                            :style {:marginRight 10}})
                                     (text {:style {:fontSize 15
                                                    :color fg-color}}
                                           "Pass list")))))

(defc drawer-row-change-path
  [state]
  (let [m @state
        selected-option (get m :selected-menu :pass-list)
        [bg-color fg-color] (if (= :change-path selected-option)
                              ["#eeeeee" "#009688"]
                              ["white" "black"])
        raw-folder (get m :folder)
        folder (->> raw-folder
                    (join "/")
                    (str "/"))]
    (touchable-native-feedback
      {:onPress (fn [] (dispatch! state :change-directory-init raw-folder))}
      (view {:flexDirection "row"
             :alignItems "center"
             :style {:padding 16
                     :paddingLeft 16
                     :backgroundColor bg-color}}
            (icon {:name "folder"
                   :size 25
                   :color fg-color
                   :style {:marginRight 10}})
            (text {:style {:fontSize 15
                           :color fg-color}}
                  "Change path")))))

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
    (drawer-row-pass-list state)
    (drawer-row-change-path state)))

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
