'use strict';

angular.module('milestogo')
    .service('ProfileService', function ProfileService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            me: function () {
                return $http.get(baseUrl + "profiles/me");
            }
        };
    });
