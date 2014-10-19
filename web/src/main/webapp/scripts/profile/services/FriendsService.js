'use strict';

angular.module('miles2run-profile')
    .service('FriendsService', function ActivityService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            followers: function (username) {
                return $http.get(baseUrl + 'profiles/' + username + '/followers');
            },
            following: function (username) {
                return $http.get(baseUrl + 'profiles/' + username + '/following');
            }
        };
    });
