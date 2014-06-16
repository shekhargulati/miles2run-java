'use strict';

function CreateGoalCtrl($scope, $location, activeProfile, $http, ConfigService) {

    $scope.currentUser = activeProfile;

    $scope.goal = {

    };

    $scope.createGoal = function () {
        console.log($scope.goal);
        $http.post(ConfigService.getBaseUrl() + "goals", $scope.goal).success(function (data) {
            toastr.success("Create new goal");
            $location.path("/");
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

    $scope.open = function ($event) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope.opened = true;
    };

    $scope.dateOptions = {
        'year-format': "'yy'",
        'starting-day': 1
    };

    $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'shortDate'];
    $scope.format = $scope.formats[0];
}

angular.module('miles2run-home')
    .controller('CreateGoalCtrl', CreateGoalCtrl);
