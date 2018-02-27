(ns pass-android.android.actions
  (:require [clojure.string :refer [join split]]))

(def fs (js/require "react-native-fs"))

(defmulti dispatch!
  (fn [state k & rest]
    k)
  :default :not-found)

(defmethod dispatch! :not-found
  [_ k & _]
  (println "Action" k "not found"))

(defmethod dispatch! :refresh-files
  [state _]
  (let [m @state
        folder (join "/" (get m :folder))
        p-read (.readDir fs folder)]
    (swap! state assoc :refreshing true)
    (-> p-read
        (.then #(swap! state assoc :files
                       (->> (js->clj % :keywordize-keys true)
                            (filter (fn [x] ((get x :isFile))))
                            (filter (fn [{:keys [name]}] (re-find #"\.pass$" name)))
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

(defn- change-folder
  [state change-folder-fn]
  (let [m @state
        old-folder (get-in m [:change-directory :directory])
        new-folder (change-folder-fn old-folder)]
    (swap! state #(-> %
                      (assoc :drawer-open? false)
                      (assoc-in [:change-directory :loading?] true)
                      (assoc-in [:change-directory :directory] new-folder)))
    (-> (.readDir fs (join "/" new-folder))
        (.then (fn [x] (swap! state #(-> %
                                         (assoc-in [:change-directory :loading?] false)
                                         (assoc-in [:change-directory :content] (js->clj x :keywordize-keys true)))))))))
(defmethod dispatch! :change-directory-above
  [state _]
  (change-folder state butlast))
(defmethod dispatch! :change-directory-folder
  [state _ item]
  (change-folder state #(vec (concat % [(:name item)]))))
(defmethod dispatch! :change-directory-init
  [state _ folder]
  (change-folder state #(-> folder)))
(defmethod dispatch! :change-directory-cancel
  [state _]
  (swap! state dissoc :selected-menu :change-directory))
(defmethod dispatch! :change-directory-accept
  [state _]
  (let [m @state
        new-folder (get-in m [:change-directory :directory])]
    (swap! state #(-> %
                      (assoc :folder new-folder)
                      (dissoc :change-directory)))))
