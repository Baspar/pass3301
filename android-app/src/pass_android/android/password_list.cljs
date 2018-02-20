(ns pass-android.android.password-list
  (:require-macros [rum.core :refer [defc]])
  (:require [re-natal.support :as support]
            [clojure.string :refer [split join lower-case]]
            [pass-android.android.utils :refer [create-element hex-alph]]
            [goog.crypt.base64 :as b64]
            [rum.core :as rum]))

(def fs (js/require "react-native-fs"))
(def ReactNative (js/require "react-native"))
(def Crypto (js/require "crypto-js"))
(def MaterialIcons (js/require "react-native-vector-icons/MaterialIcons"))
(def ActionButton (.-default (js/require "react-native-action-button")))

(def icon (partial create-element (.-default MaterialIcons)))
(def action-button (partial create-element ActionButton))
(def view (partial create-element (.-View ReactNative)))
(def text (partial create-element (.-Text ReactNative)))
(def text-input (partial create-element (.-TextInput ReactNative)))
(def image (partial create-element (.-Image ReactNative)))
(def flat-list (partial create-element (.. ReactNative -FlatList)))
(def clipboard (.-Clipboard ReactNative))
(def toast (.-ToastAndroid ReactNative))
(def touchable-native-feedback (partial create-element (.-TouchableNativeFeedback ReactNative)))
(def touchable-opacity (partial create-element (.-TouchableOpacity ReactNative)))
(def selectable-background (.. ReactNative -TouchableNativeFeedback SelectableBackground))

(defn refresh-files [state]
  (let [m @state
        folder (get m :folder)
        p-read (.readDir fs folder)]
    (swap! state assoc :refreshing true)
    (-> p-read
        (.then #(swap! state assoc :files
                       (->> (js->clj % :keywordize-keys true)
                            (filter (fn [x] ((get x :isFile))))
                            (mapv (fn [{:keys [path name]}]
                                    (let [fancy-name (->> (split name #"\.")
                                                          (butlast)
                                                          (join "."))]
                                      {:path path
                                       :key name
                                       :name fancy-name
                                       :filename name}))))
                       :refreshing false))
        (.catch #(swap! state assoc :refreshing false)))))

(defc render-row
  [state x]
  (let [item (js->clj (.-item x) :keywordize-keys true)
        {:keys [path name]} item]
    (touchable-native-feedback {:background selectable-background
                                :onPress #(swap! state assoc
                                                 :file-to-decrypt path
                                                 :file-to-decrypt-name name)}
                               (view {:style {:padding 20
                                              :borderBottomWidth 1}}
                                     (text name)))))

(defc no-file-placeholder
  [state]
  (view {:style {:flex 1
                 :justifyContent "center"
                 :alignItems "center"}}
        (text {:style {:opacity 0.5
                       :fontSize 30}}
              "No passfiles found :(")
        (touchable-native-feedback {:background selectable-background
                                    :onPress #(refresh-files state)}
                                   (view {:elevation 5
                                          :style {:backgroundColor "#009688"
                                                  :borderRadius 5
                                                  :marginTop 30
                                                  :padding 10}}
                                         (text {:style {:color "white"
                                                        :fontSize 20
                                                        :textAlign "center"}}
                                               "Refresh")))))

(defn matches
  [string pattern]
  (cond
    (empty? string) false
    (empty? pattern) true
    :else (if (= (lower-case (first string))
                 (lower-case (first pattern)))
            (recur (rest string) (rest pattern))
            (recur (rest string) pattern))))
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
            [(text {:style {:fontSize 25
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
                         (filter (fn [{:keys [name]}] (matches name search))
                                 files)
                         files)
        refreshing (get m :refreshing false)]
    (view {:style {:flexDirection "column"
                   :flex 1}}
          (password-list-header state)
          (if (empty? files)
            (no-file-placeholder state)
            (flat-list {:data filtered-files
                        :onRefresh #(refresh-files state)
                        :refreshing refreshing
                        :ListEmptyComponent (text {:style {:fontSize 30
                                                           :paddingTop 50
                                                           :textAlign "center"
                                                           :color "lightgrey"}}
                                                  "No result")
                        :renderItem #(render-row state %)}))
          (action-button {:buttonColor "#009688"
                          :size 60
                          :onPress #(println MaterialIcons)}))))
