(ns pass-android.android.state)

(def fs (js/require "react-native-fs"))

(defonce app-state
  (atom {:folder [(.-ExternalStorageDirectoryPath fs)
                  ".pass"]
         :files []}))

(defonce refs
  (atom {}))
