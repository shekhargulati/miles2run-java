'use strict';

angular.module('milestogo')
    .service('ActivityService', function ActivityService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            postActivity: function (data) {
                return $http.post(baseUrl + "activities", data);
            },
            get: function (activityId) {
                return $http.get(baseUrl + "activities/" + activityId);
            },

            updateActivity: function (activityId, data) {
                return $http.put(baseUrl + "activities/" + activityId, data);
            },
            deleteActivity: function (activityId) {
                return $http.delete(baseUrl + "activities/" + activityId);
            },
            shareActivity: function (activityId, data) {
                return $http.put(baseUrl + "activities/" + activityId + "/share", data);
            }

        };
    });
