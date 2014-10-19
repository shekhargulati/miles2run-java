'use strict';

angular.module('miles2run-home')
    .service('ActivityService', function ActivityService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            postActivity: function (data, goalId) {
                return $http.post(baseUrl + "goals/" + goalId + "/activities", data);
            },
            get: function (activityId, goalId) {
                return $http.get(baseUrl + "goals/" + goalId + "/activities/" + activityId);
            },

            updateActivity: function (activityId, data, goalId) {
                return $http.put(baseUrl + "goals/" + goalId + "/activities/" + activityId, data);
            },
            deleteActivity: function (activityId, goalId) {
                return $http.delete(baseUrl + "goals/" + goalId + "/activities/" + activityId);
            },
            shareActivity: function (activityId, data, goalId) {
                return $http.put(baseUrl + "goals/" + goalId + "/activities/" + activityId + "/share", data);
            }

        };
    });
