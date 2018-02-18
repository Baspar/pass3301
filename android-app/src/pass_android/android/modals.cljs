(ns pass-android.android.modals
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.utils :refer [create-element]]
            [rum.core :as rum]))

(def ReactNative (js/require "react-native"))

(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def modal (partial create-element (.-Modal ReactNative)))
(def toast (.-ToastAndroid ReactNative))
(def touchable-native-feedback (partial create-element (.-TouchableNativeFeedback ReactNative)))
(def selectable-background (.. ReactNative -TouchableNativeFeedback SelectableBackground))

(defc password-modal
  [state]
  (let [m @state
        file-to-decrypt (get m :file-to-decrypt false)
        close-fn (fn [] (swap! state dissoc :file-to-decrypt))]
    (modal {:visible (if file-to-decrypt true false)
            :animationType "slide"
            :transparent true
            :onRequestClose close-fn}
           (view {:flex 1
                  :backgroundColor "rgba(0,0,0,0.6)"
                  :justifyContent "center"
                  :alignItems "center"}
                 (view {:backgroundColor "white"
                        :padding 40}
                       (text "Hey"))))))
