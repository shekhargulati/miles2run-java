'use strict';

angular.module('miles2run-home')
    .controller('NotificationsCtrl', function ($scope, $http, activeProfile, ConfigService) {

        $http.get(ConfigService.getBaseUrl() + 'profiles/' + activeProfile.username + "/notifications").success(function (data, status, headers, config) {
            $scope.notifications = data;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch notifications. Please try later");
        });

        $scope.appContext = function () {
            return ConfigService.appContext();
        }
    });
