(ns lightair
  (:use midje.sweet)
  (:import net.sf.lightair.Api))

(Api/initialize "dev-resources/light-air.properties")

(defn- process-files
  [prefix files]
  (vec (map #(str "test/it/" prefix %1) files)))

(defn db-setup
  [prefix & files]
  (Api/setup {Api/DEFAULT_PROFILE, (process-files prefix files)}))

(defn db-verify
  [prefix & files]
  (Api/verify {Api/DEFAULT_PROFILE, (process-files prefix files)})
  '((try
      (catch AssertionError e
        (fact "Database verification failed." (.getMessage e) => nil)
        )))
  )
