(ns pass-android.android.password-list
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [clojure.string :refer [split join lower-case]]
            [pass-android.android.utils :refer [string-matches create-element hex-alph]]
            [pass-android.android.actions :refer [dispatch!]]
            [goog.crypt.base64 :as b64]
            [rum.core :as rum]))

(def fs (js/require "react-native-fs"))
(def ReactNative (js/require "react-native"))
(def MaterialIcons (js/require "react-native-vector-icons/MaterialIcons"))
(def ActionButton (.-default (js/require "react-native-action-button")))

(def icon (partial create-element (.-default MaterialIcons)))
(def action-button (partial create-element ActionButton))
(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def activity-indicator (partial create-element (.-ActivityIndicator ReactNative)))
(def text-input (partial create-element (.-TextInput ReactNative)))
(def image (partial create-element (.-Image ReactNative)))
(def flat-list (partial create-element (.. ReactNative -FlatList)))
(def clipboard (.-Clipboard ReactNative))
(def toast (.-ToastAndroid ReactNative))
(def touchable-native-feedback (partial create-element (.-TouchableNativeFeedback ReactNative)))
(def touchable-opacity (partial create-element (.-TouchableOpacity ReactNative)))
(def selectable-background (.. ReactNative -TouchableNativeFeedback SelectableBackground))


(defc render-row
  [state x]
  (let [item (js->clj (.-item x) :keywordize-keys true)
        {:keys [path name]} item]
    (touchable-native-feedback {:background selectable-background
                                :onPress #(swap! state assoc
                                                 :file-to-decrypt path
                                                 :file-to-decrypt-name name)}
                               (view {:flexDirection "row"
                                      :alignItems "center"}
                                     (icon {:name "lock"
                                            :style {:paddingLeft 10}
                                            :size 30})
                                 (view {:style {:padding 20}}
                                       (text name))))))

(defc no-file-placeholder
  [state]
  (view {:style {:flex 1
                 :justifyContent "center"
                 :alignItems "center"}}
        (text {:style {:opacity 0.5
                       :fontSize 30}}
              "No passfiles found :(")
        (text {:style {:opacity 0.5
                       :fontSize 20}}
              "Pull to refresh")))

(defc password-list-header
  [state]
  (let [m @state
        searching? (get m :searching false)
        search (get m :search "")
        search? (and search (not (empty? search)))]
    (view {:style {:paddingLeft 15
                   :paddingRight 15
                   :height 70
                   :backgroundColor "#009688"
                   :flexDirection "row"
                   :alignItems "center"}
           :elevation 5}


          (if searching?
            [(touchable-opacity {:onPress #(swap! state dissoc :searching :search)
                                 :background selectable-background}
                                (icon {:name "arrow-back"
                                       :color "white"
                                       :size 30}))
             (text-input {:style {:fontSize 25
                                  :marginLeft 10
                                  :color "white"
                                  :marginRight 10}
                          :onChangeText (fn [text] (swap! state assoc :search text))
                          :flex 1
                          :value search
                          :spellCheck false
                          :selectionColor "white"
                          :placeholderTextColor "rgba(255, 255, 255, 0.3)"
                          :underlineColorAndroid "transparent"
                          :returnKeyType "search"
                          :autoFocus true
                          :placeholder " Search"})
             (when search?
               (touchable-opacity {:onPress #(swap! state assoc :search "")
                                   :background selectable-background}
                                  (icon {:name "close"
                                         :color "white"
                                         :size 30})))]
            [(touchable-opacity {:onPress #(swap! state assoc :drawer-open? true)
                                 :background selectable-background}
                                (icon {:name "menu"
                                       :color "white"
                                       :size 30}))
             (text {:style {:fontSize 25
                            :paddingLeft 15
                            :paddingTop 10
                            :paddingBottom 10
                            :flex 1
                            :color "white"}}
                   "Pass3301")
             (touchable-opacity {:onPress #(swap! state assoc :searching true)
                                 :background selectable-background}
                                (icon {:name "search"
                                       :color "white"
                                       :size 30}))]))))
(defc password-list-page
  [state]
  (let [m @state
        files (get m :files [])
        folder (get m :folder [])
        searching? (get m :searching false)
        search (get m :search "")
        filtered-files (if searching?
                         (filter (fn [{:keys [name]}] (string-matches name search))
                                 files)
                         files)
        refreshing (get m :refreshing false)]
    (view {:style {:flexDirection "column"
                   :flex 1}}
          (password-list-header state)
          (flat-list {:data filtered-files
                      :onRefresh #(dispatch! state :refresh-files)
                      :refreshing refreshing
                      :contentContainerStyle (if (empty? filtered-files)
                                               {:flexGrow 1 :justifyContent "center"}
                                               {})
                      :ListEmptyComponent (no-file-placeholder state)
                      :renderItem #(render-row state %)})
          (action-button {:buttonColor "#009688"
                          :size 60
                          :onPress #(swap! state assoc :new-pass {})}))))
