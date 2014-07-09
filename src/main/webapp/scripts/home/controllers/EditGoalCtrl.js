'use strict';

angular.module('miles2run-home')
    .controller('EditGoalCtrl', function ($scope, $http, ConfigService, $routeParams, $location) {

        $scope.buttonText = "Update";

        $http.get(ConfigService.getBaseUrl() + "goals/" + $routeParams.goalId).success(function (data) {
            $scope.goal = data;
            $scope.today();
        }).error(function (data, status) {
            toastr.error("Unable to fetch goal. Please try after sometime.");
            console.log("Error " + data);
            console.log("Status " + status)
        });

        $scope.editGoal = function () {
            $scope.submitted = true;
            if ($scope.goalForm.$valid) {
                $scope.successfulSubmission = true;
                $scope.buttonText = "Updating Goal..";
                var goal = {
                    id: $scope.goal.id,
                    purpose: $scope.goal.purpose,
                    startDate: $scope.goal.startDate,
                    endDate: $scope.goal.endDate,
                    distance: $scope.goal.distance,
                    goalUnit: $scope.goal.goalUnit,
                    archived: $scope.goal.archived
                }
                $scope.editGoalPromise =  $http.put(ConfigService.getBaseUrl() + "goals/" + $scope.goal.id, goal).success(function (data) {
                    toastr.success("Updated goal");
                    $location.path("/");
                }).error(function (data, status) {
                    toastr.error("Unable to update goal. Please try after sometime.");
                    console.log("Error " + data);
                    console.log("Status " + status)
                    $location.path("/");
                });
            }

        };

        $scope.today = function () {
            $scope.goal.targetDate = new Date($scope.goal.targetDate);
        };

        $scope.showWeeks = true;
        $scope.toggleWeeks = function () {
            $scope.showWeeks = !$scope.showWeeks;
        };

        $scope.clear = function () {
            $scope.goal.targetDate = null;
        };

        // Disable weekend selection
        $scope.disabled = function (date, mode) {
            return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
        };

        $scope.toggleMin = function () {
            $scope.minDate = ( $scope.minDate ) ? null : new Date();
        };
        $scope.toggleMin();

        $scope.openStartDate = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.openedStartDate = true;
        };

        $scope.openEndDate = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.openedEndDate = true;
        };

        $scope.dateOptions = {
            'year-format': "'yy'",
            'starting-day': 1
        };

    });