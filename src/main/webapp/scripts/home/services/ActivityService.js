'use strict';

angular.module('milestogo')
    .service('ActivityService', function ActivityService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            postActivity: function (username, data) {
                return $http.post(baseUrl + "profiles/" + username + "/activities", data);
            },
            timeline: function (username) {
                return $http.get(baseUrl + "profiles/" + username + "/activities");
            },
            get: function (username, activityId) {
                return $http.get(baseUrl + "profiles/" + username + "/activities/" + activityId);
            },

            updateActivity: function (username, activityId, data) {
                return $http.put(baseUrl + "profiles/" + username + "/activities/" + activityId, data);
            },
            deleteActivity: function (username, activityId) {
                return $http.delete(baseUrl + "profiles/" + username + "/activities/" + activityId);
            },
            shareActivity: function (username, activityId, data) {
                return $http.put(baseUrl + "profiles/" + username + "/activities/" + activityId + "/share", data);
            }

        };
    });
