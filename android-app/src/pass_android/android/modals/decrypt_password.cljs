(ns pass-android.android.modals.decrypt-password
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [pass-android.android.utils :refer [create-element hex-alph decrypt]]
            [goog.crypt.base64 :as b64]
            [rum.core :as rum]))

(def fs (js/require "react-native-fs"))
(def ReactNative (js/require "react-native"))

(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def modal (partial create-element (.-Modal ReactNative)))
(def text-input (partial create-element (.-TextInput ReactNative)))
(def toast (.-ToastAndroid ReactNative))
(def clipboard (.-Clipboard ReactNative))
(def touchable-native-feedback (partial create-element (.-TouchableOpacity ReactNative)))

(defn on-submit-password
  [state]
  (let [m @state
        path (get m :file-to-decrypt)
        password (get m :password "")
        password? (and password (not (empty? password)))]
    (when password?
      (.then (.readFile fs path "utf8")
             (fn [d] (let [x (-> d
                                 (decrypt password)
                                 str)
                           mesg (if (empty? x)
                                  x
                                  (->> x
                                       (partition 2)
                                       butlast
                                       (map (fn [[a b]] (+ (* 16 (hex-alph a))
                                                           (hex-alph b))))
                                       (map char)
                                       (apply str)))
                           error? (empty? mesg)]
                       (if error?
                         (swap! state #(-> %
                                           (assoc :error "Wrong password")
                                           (dissoc :password)))
                         (do
                           (.setString clipboard mesg)
                           (.show toast "Password copied for 5s" (.-SHORT toast))

                           (.setTimeout js/window #(do
                                                     (.show toast "Password cleared" (.-SHORT toast))
                                                     (.setString clipboard ""))
                                        5000)

                           (swap! state dissoc :password :file-to-decrypt :file-to-decrypt-name))))))
      )))
(defc password-modal-header
  [state]
  (let [m @state
        file-to-decrypt (get m :file-to-decrypt false)
        file-to-decrypt-name (get m :file-to-decrypt-name "")]
    (view {:backgroundColor "#009688"
           :padding 20
           :flexDirection "row"}
          (text {:style {:fontSize 20
                         :fontWeight "500"
                         :color "white"}}
                "Password for ")
          (text {:style {:fontSize 20
                         :color "white"
                         :opacity 0.5}}
                file-to-decrypt-name))))
(defc password-modal-buttons
  [state]
  (let [m @state
        password (get m :password false)
        password? (and password (not (empty? password)))]
    (view {:flexDirection "row"
           :padding 20
           :paddingTop 0
           :justifyContent "flex-end"}
          (touchable-native-feedback {:onPress #(swap! state dissoc :password :file-to-decrypt)}
                                     (text {:style {:margin 10
                                                    :fontSize 20
                                                    :fontWeight "bold"
                                                    :color "#009688"}}
                                           "Cancel"))
          (touchable-native-feedback {:disabled (not password?)
                                      :onPress #(on-submit-password state)}
                                     (text {:style {:margin 10
                                                    :fontSize 20
                                                    :fontWeight "bold"
                                                    :color (if password? "#009688" "lightgrey")}}
                                           "OK")))))
(defc password-modal-input
  [state]
  (let [m @state
        error (get m :error "")
        error? (and error (not (empty? error)))
        password (get m :password "")]
    (view {:padding 20
           :paddingTop 30}
          (text-input {:placeholder (if error? error "Password")
                       :placeholderTextColor (if error? "red" "lightgrey")
                       :secureTextEntry true
                       :autoFocus true
                       :value password
                       :underlineColorAndroid (if error? "red" "#009688")
                       :onChangeText (fn [password] (swap! state #(-> %
                                                                      (dissoc :error)
                                                                      (assoc :password password))))
                       :style {:fontSize 17}}))))
(defc password-modal
  [state]
  (let [m @state
        password (get m :password false)
        password? (and password (not (empty? password)))
        file-to-decrypt (get m :file-to-decrypt false)
        file-to-decrypt-name (get m :file-to-decrypt-name "")
        close-fn (fn [] (swap! state dissoc :file-to-decrypt))]
    [(modal {:visible (if file-to-decrypt true false)
             :animationType "fade"
             :transparent true
             :onRequestClose close-fn}
            (view {:flex 1
                   :backgroundColor "rgba(0,0,0,0.5)"}))
     (modal {:visible (if file-to-decrypt true false)
             :animationType "slide"
             :transparent true
             :onRequestClose close-fn}
            (view {:flex 1
                   :justifyContent "center"}
                  (view {:backgroundColor "white"
                         :margin 20
                         :elevation 5}
                        (password-modal-header state)
                        (password-modal-input state)
                        (password-modal-buttons state))))]))
