'use strict';

angular.module('milestogo')
    .service('ProgressService', function ProgressService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            progress: function (username) {
                return $http.get(baseUrl + "profiles/" + username + "/progress");
            }
        };
    });
