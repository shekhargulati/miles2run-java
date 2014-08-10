'use strict';

function CreateDistanceGoalCtrl($scope, $location, activeProfile, $http, ConfigService, $window) {

    $scope.currentUser = activeProfile;

    $scope.buttonText = "Create";

    $scope.goal = {
        distance: 100,
        goalUnit: 'MI',
        purpose: 'Run for 100 miles'
    };

    $scope.createDistanceGoal = function () {
        $scope.submitted = true;
        if ($scope.goalForm.$valid) {
            createGoal('DISTANCE_GOAL');
        }
    }

    var createGoal = function (goalType) {
        $scope.successfulSubmission = true;
        $scope.buttonText = "Creating Goal..";
        $scope.goal.goalType = goalType;
        $scope.createGoalPromise = $http.post(ConfigService.getBaseUrl() + "goals", $scope.goal).success(function (data) {
            toastr.success("Created new goal");
            $window.location.href = ConfigService.appContext() + 'goals/' + data.id;
        }).error(function (data, status) {
            toastr.error("Unable to create goal. Please try after sometime.");
            console.log("Error " + data);
            console.log("Status " + status)
            $location.path("/");
        });

    };

    $scope.today = function () {
        $scope.dt = new Date();
    };
    $scope.today();

    $scope.showWeeks = true;
    $scope.toggleWeeks = function () {
        $scope.showWeeks = !$scope.showWeeks;
    };

    $scope.clear = function () {
        $scope.dt = null;
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

}

angular.module('miles2run-home')
    .controller('CreateDistanceGoalCtrl', CreateDistanceGoalCtrl);
