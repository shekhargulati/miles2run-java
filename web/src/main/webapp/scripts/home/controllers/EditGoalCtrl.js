'use strict';

angular.module('miles2run-home')
    .controller('EditGoalCtrl', function ($scope, $http, ConfigService, $routeParams, $location) {

        $scope.buttonText = "Update";

        $http.get(ConfigService.getBaseUrl() + "goals/" + $routeParams.goalId).success(function (data) {
            $scope.goal = data;
            if ($scope.goal.startDate && $scope.goal.endDate) {
                $scope.goal.numberOfDays = moment($scope.goal.endDate).diff($scope.goal.startDate, 'days') + 1;
                if ($scope.goal.goalType === "COMMUNITY_RUN_GOAL") {
                    $scope.minStartDate = new Date($scope.goal.communityRunStartDate);
                    $scope.maxEndDate = new Date($scope.goal.communityRunEndDate)
                }

            }
            $scope.today();
        }).error(function (data, status) {
            toastr.error("Unable to fetch goal. Please try after sometime.");
            console.log("Error " + data);
            console.log("Status " + status)
        });

        $scope.editDistanceGoal = function () {
            $scope.submitted = true;
            if ($scope.goalForm.$valid) {
                editGoal('DISTANCE_GOAL');
            }
        }

        $scope.validateDateRange = function (startDate, endDate) {
            if (startDate && endDate) {
                if (startDate.getTime() < endDate.getTime()) {
                    $scope.goalForm.startDate.$invalid = false;
                } else {
                    $scope.goalForm.startDate.$invalid = true;
                }
            }
        }

        $scope.editDurationGoal = function () {
            $scope.submitted = true;
            $scope.validateDateRange($scope.goal.startDate, $scope.goal.endDate);
            if ($scope.goalForm.$valid && !$scope.goalForm.startDate.$invalid) {
                editGoal('DURATION_GOAL');
            }
        }

        var editGoal = function (goalType) {
            $scope.successfulSubmission = true;
            $scope.buttonText = "Updating Goal..";
            var goal = {
                purpose: $scope.goal.purpose,
                startDate: $scope.goal.startDate,
                endDate: $scope.goal.endDate,
                distance: $scope.goal.distance,
                goalUnit: $scope.goal.goalUnit,
                archived: $scope.goal.archived,
                goalType: goalType
            }
            $scope.editGoalPromise = $http.put(ConfigService.getBaseUrl() + "goals/" + $scope.goal.id, goal).success(function (data) {
                toastr.success("Updated goal");
                $location.path("/");
            }).error(function (data, status) {
                toastr.error("Unable to update goal. Please try after sometime.");
                console.log("Error " + data);
                console.log("Status " + status)
                $location.path("/");
            });
        };

        $scope.today = function () {
            $scope.goal.startDate = $scope.goal.startDate ? new Date($scope.goal.startDate) : null;
            $scope.goal.endDate = $scope.goal.endDate ? new Date($scope.goal.endDate) : null;
        };

        $scope.showWeeks = true;
        $scope.toggleWeeks = function () {
            $scope.showWeeks = !$scope.showWeeks;
        };

        $scope.clear = function () {
            $scope.goal.startDate = null;
            $scope.goal.endDate = null;
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

        $scope.numberOfDaysUpdated = function () {
            if ($scope.goal.numberOfDays) {
                $scope.goal.endDate = addDays($scope.goal.startDate, $scope.goal.numberOfDays);
            }
        }

        $scope.startDateUpdated = function () {
            $scope.goal.numberOfDays = moment($scope.goal.endDate).diff($scope.goal.startDate, 'days') + 1;
        }

        $scope.endDateUpdated = function () {
            $scope.goal.numberOfDays = moment($scope.goal.endDate).diff($scope.goal.startDate, 'days') + 1;
        }

        function addDays(date, days) {
            var result = new Date(date);
            result.setDate(date.getDate() + (days - 1));
            return result;
        }

    });