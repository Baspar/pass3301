(ns pass-android.android.modals.change-directory
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.utils :refer [create-element hex-alph decrypt]]
            [goog.crypt.base64 :as b64]
            [rum.core :as rum]))

(def ReactNative (js/require "react-native"))
(def fs (js/require "react-native-fs"))
(def MaterialIcons (js/require "react-native-vector-icons/MaterialIcons"))

(def icon (partial create-element (.-default MaterialIcons)))
(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def modal (partial create-element (.-Modal ReactNative)))
(def text-input (partial create-element (.-TextInput ReactNative)))
(def toast (.-ToastAndroid ReactNative))
(def touchable-opacity (partial create-element (.-TouchableOpacity ReactNative)))

(defc change-directory-header
  [state]
  (let [close-fn #(swap! state dissoc :selected-menu)]
    (view {:style {:paddingLeft 15
                   :paddingRight 15
                   :height 70
                   :backgroundColor "#009688"
                   :flexDirection "row"
                   :alignItems "center"}
           :elevation 5}
          (touchable-opacity {:onPress close-fn}
                             (icon {:name "arrow-back"
                                    :color "white"
                                    :size 30}))
          (text {:style {:fontSize 25
                         :paddingLeft 15
                         :paddingTop 10
                         :paddingBottom 10
                         :flex 1
                         :color "white"}}
                "Press back")
          (touchable-opacity {:onPress close-fn}
                             (icon {:name "done"
                                    :color "white"
                                    :size 30})))))
(defc change-directory-main
  [state]
  (view {:flex 1
         :backgroundColor "white"}
        (text "Press back")))
(defc change-directory-modal
  [state]
  (let [m @state
        visible? (= "change-path" (get m :selected-menu "pass-list"))
        close-fn #(swap! state dissoc :selected-menu)]
    [(modal {:visible visible?
             :animationType "fade"
             :transparent true
             :onRequestClose close-fn}
            (view {:flex 1
                   :backgroundColor "rgba(0,0,0,0.5)"}))
     (modal {:visible visible?
             :animationType "slide"
             :transparent true
             :onRequestClose close-fn}
            (change-directory-header state)
            (change-directory-main state))]))
