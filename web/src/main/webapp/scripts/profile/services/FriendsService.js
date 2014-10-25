'use strict';

angular.module('miles2run-profile')
    .service('FriendsService', function ActivityService($http, ConfigService) {
        var baseUrl = ConfigService.getBaseUrl();
        return {
            followers: function (username) {
                return $http.get(baseUrl + 'users/' + username + '/followers');
            },
            following: function (username) {
                return $http.get(baseUrl + 'users/' + username + '/following');
            }
        };
    });
