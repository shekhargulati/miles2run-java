'use strict';

angular.module('milestogo')
    .service('ProgressService', function ProgressService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            progress: function (goalId) {
                return $http.get(baseUrl + "goals/" + goalId + "/progress");
            }
        };
    });
