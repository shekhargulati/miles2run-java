'use strict';

angular.module('miles2run-profile')
    .service('TimelineService', function ActivityService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            userGoalTimeline: function (username, page) {
                return $http.get(baseUrl + "activities/user_timeline", {params: {username: username, page: page}});
            }
        };
    });
