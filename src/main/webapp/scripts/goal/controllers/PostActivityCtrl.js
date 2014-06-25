'use strict';

function PostActivityCtrl($scope, ActivityService, $location, ProfileService, activeProfile, $rootScope, activeGoal) {

    $scope.currentUser = activeProfile;
    $scope.activeGoal = activeGoal;
    $scope.activity = {
        goalUnit: $scope.activeGoal.goalUnit.$name,
        share: {}
    };

    $scope.duration = {
        hours: "00",
        minutes: "00",
        seconds: "00"
    };


    $scope.validateDuration = function (duration) {
        var durationVal = toAppSeconds(duration);
        if (durationVal > 0) {
            $scope.activityForm.durationHours.$invalid = false;
            $scope.activityForm.durationMinutes.$invalid = false;
            $scope.activityForm.durationSeconds.$invalid = false;
        }
    }

    $scope.postActivity = function (isValid) {
        $scope.submitted = true;
        var duration = toAppSeconds($scope.duration);
        if (duration === 0) {
            $scope.activityForm.durationHours.$invalid = true;
            $scope.activityForm.durationMinutes.$invalid = true;
            $scope.activityForm.durationSeconds.$invalid = true;
            isValid = false;
        } else {
            $scope.activityForm.durationHours.$invalid = false;
            $scope.activityForm.durationMinutes.$invalid = false;
            $scope.activityForm.durationSeconds.$invalid = false;
        }
        if (isValid) {
            $scope.activity.duration = duration;
            ActivityService.postActivity($scope.activity, activeGoal.id).success(function (data, status, headers, config) {
                $rootScope.$broadcast('update.progress', 'true');
                toastr.success("Posted new activity");
                $location.path('/');
            }).error(function (data, status, headers, config) {
                console.log("Error handler for PostActivity. Status code " + status);
                toastr.error("Unable to post activity. Please try later.");
                $location.path('/');
            });
        }


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

function toAppSeconds(duration) {
    if (duration) {
        var hours = duration.hours && duration.hours !== '00' ? duration.hours : 0;
        var minutes = duration.minutes && duration.minutes !== '00' ? duration.minutes : 0;
        var seconds = duration.seconds && duration.seconds !== '00' ? duration.seconds : 0;
        return Number(hours) * 60 * 60 + Number(minutes) * 60 + Number(seconds);
    }
    return 0;
}
angular.module('milestogo')
    .controller('PostActivityCtrl', PostActivityCtrl);
