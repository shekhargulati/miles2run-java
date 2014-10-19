'use strict';

angular.module('miles2run-home')
    .controller('NotificationsCtrl', function ($scope, $http, activeProfile, ConfigService) {

        $scope.notificationsPromise = $http.get(ConfigService.getBaseUrl() + "notifications").success(function (data, status, headers, config) {
            $scope.notifications = data;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch notifications. Please try later");
        });

        $scope.appContext = function () {
            return ConfigService.appContext();
        }
    });
