(ns bob-poc.routes.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [clojure.tools.logging :refer [info]]
            [bob-poc.routes.resource-routes :refer [resource-routes]]
            [bob-poc.routes.match-routes :refer [match-routes]]
            [bob-poc.routes.band-routes :refer [band-routes]]
            [bob-poc.application.properties :refer [current-env]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]))

(defn- wrap-dir-index
  "Middleware to force request for / to return index.html"
  [handler]
  (fn [req]
    (info (:uri req))
    (handler (update-in req [:uri] #(if (= "/" %) "/index.html" (if (= "/callback" %) "/callback.html" %))))))

(defn- wrap-dev-cors-support
  "Used to allow CORS requests in dev environment because front is running with Webpack in different port"
  [handler]
  (fn [req]
    (if (= (current-env) "dev-test")
      (let [response (handler req)]
        (-> response
            (assoc-in [:headers "Access-Control-Allow-Origin"]  "*")
            (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,PUT,POST,DELETE,OPTIONS")
            (assoc-in [:headers "Access-Control-Allow-Headers"] "X-Requested-With,Content-Type,Cache-Control")))
      (handler req))))

(def app
  (-> (handler/api (routes match-routes band-routes))
      wrap-dir-index
      wrap-dev-cors-support
      (wrap-json-body {:keywords? true})
      wrap-json-response))
