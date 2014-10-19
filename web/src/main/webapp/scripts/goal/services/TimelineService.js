'use strict';

angular.module('milestogo')
    .service('TimelineService', function ActivityService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            homeTimeline: function (page) {
                return $http.get(baseUrl + "activities/home_timeline", {params: {page: page}});
            },
            userTimeline: function (username, page) {
                return $http.get(baseUrl + "activities/user_timeline", {params: {username: username, page: page}});
            },
            goalTimeline: function (goalId, page) {
                return $http.get(baseUrl + "goals/" + goalId + "/activities/goal_timeline", {params: {page: page}});
            },
            userGoalTimeline: function (username, goalId, page) {
                return $http.get(baseUrl + "goals/" + goalId + "/activities/user_goal_timeline", {params: {username: username, page: page}});
            }
        };
    });
