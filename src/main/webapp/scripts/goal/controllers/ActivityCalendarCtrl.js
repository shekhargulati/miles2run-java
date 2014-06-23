'use strict';

angular.module('milestogo')
    .controller('ActivityCalendarCtrl', function ($scope, ConfigService, activeProfile, activeGoal) {
        $scope.currentUser = activeProfile;
        $scope.activeGoal = activeGoal;
        $scope.data = {};

        function startDate() {
            var d = new Date();
            d.setMonth(d.getMonth() - 2);
            return d;
        }

        $scope.config = {
            minDate: startDate(),
            maxDate: new Date(),
            data: ConfigService.getBaseUrl() + "goals/" + activeGoal.id + "/activities/calendar",
            itemName: [$scope.activeGoal.goalUnit.$name.toLowerCase(),$scope.activeGoal.goalUnit.$name.toLowerCase()]
        };


    });
