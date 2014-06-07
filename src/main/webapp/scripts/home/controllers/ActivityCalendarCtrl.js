'use strict';

angular.module('milestogo')
    .controller('ActivityCalendarCtrl', function ($scope, ConfigService, activeProfile) {
        $scope.currentUser = activeProfile;
        $scope.data = {};

        function startDate() {
            var d = new Date();
            d.setMonth(d.getMonth() - 2);
            return d;
        }

        $scope.config = {
            minDate: startDate(),
            maxDate: new Date(),
            data: ConfigService.getBaseUrl() + "profiles/" + $scope.currentUser.username + "/progress/timeline",
            itemName: $scope.currentUser.goalUnit.$name.toString().toLowerCase().replace("s", "")
        };


    });
