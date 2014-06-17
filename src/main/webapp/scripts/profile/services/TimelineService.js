'use strict';

angular.module('miles2run-profile')
    .service('TimelineService', function ActivityService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            userGoalTimeline: function (username, goalId, page) {
                return $http.get(baseUrl + "goals/" + goalId + "/activities/user_goal_timeline", {params: {username: username, page: page}});
            }
        };
    });
