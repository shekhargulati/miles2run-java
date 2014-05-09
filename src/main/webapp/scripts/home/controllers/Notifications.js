'use strict';

angular.module('milestogo')
    .controller('NotificationsCtrl', function ($scope, $http, activeProfile, ConfigService) {

        $http.get('api/v2/profiles/' + activeProfile.username + "/notifications").success(function (data, status, headers, config) {
            $scope.notifications = data;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch notifications. Please try later");
        });

        $scope.appContext = function(){
            return ConfigService.appContext();
        }
    });
