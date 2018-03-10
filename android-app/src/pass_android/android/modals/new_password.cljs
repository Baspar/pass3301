(ns pass-android.android.modals.new-password
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.utils :refer [create-element hex-alph]]
            [pass-android.android.actions :refer [dispatch!]]
            [goog.crypt.base64 :as b64]
            [rum.core :as rum]))

(def fs (js/require "react-native-fs"))
(def ReactNative (js/require "react-native"))
(def Crypto (js/require "crypto-js"))
(def MaterialIcons (js/require "react-native-vector-icons/MaterialIcons"))

(def icon (partial create-element (.-default MaterialIcons)))
(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def modal (partial create-element (.-Modal ReactNative)))
(def text-input (partial create-element (.-TextInput ReactNative)))
(def toast (.-ToastAndroid ReactNative))
(def touchable-opacity (partial create-element (.-TouchableOpacity ReactNative)))
(def flat-list (partial create-element (.. ReactNative -FlatList)))
(def touchable-native-feedback (partial create-element (.-TouchableNativeFeedback ReactNative)))

(defc new-password-modal
  [state]
  (let [m @state
        visible? (some? (get m :new-password))]
    [(modal {:visible visible?
             :animationType "fade"
             :transparent true
             :onRequestClose #(dispatch! state :new-password-cancel)}
            (view {:flex 1
                   :backgroundColor "rgba(0,0,0,0.5)"}))
     (modal {:visible visible?
             :animationType "slide"
             :transparent true
             :onRequestClose #(dispatch! state :new-password-cancel)}
            (view {:flex 1
                   :backgroundColor "white"})
            ;; (change-directory-header state)
            ;; (change-directory-main state)
            )]))
