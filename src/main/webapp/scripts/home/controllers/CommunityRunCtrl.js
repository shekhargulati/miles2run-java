'use strict';

function CommunityRunCtrl($scope, $location, activeProfile, $http, ConfigService, $window) {

    $scope.currentUser = activeProfile;

    $scope.buttonText = "Create";


    $scope.validateDateRange = function (startDate, endDate) {
        if (startDate && endDate) {
            if (startDate.getTime() < endDate.getTime()) {
                $scope.communityRunForm.startDate.$invalid = false;
            } else {
                $scope.communityRunForm.startDate.$invalid = true;
            }
        }
    }
    $scope.createCommunityRun = function () {
        $scope.submitted = true;
        $scope.validateDateRange($scope.communityRun.startDate, $scope.communityRun.endDate);
        if ($scope.communityRunForm.$valid && !$scope.communityRunForm.startDate.$invalid) {
            $scope.successfulSubmission = true;
            $scope.buttonText = "Creating...";
            console.log($scope.communityRun);
            var hashtagsObjArray = $scope.communityRun.hashtags;
            var hashtags = [];
            angular.forEach(hashtagsObjArray,function(value){
                hashtags.push(value['text']);
            });
            $scope.communityRun.hashtags = hashtags;
            $http.post(ConfigService.getBaseUrl() + "community_runs",$scope.communityRun).success(function (data) {
                toastr.success("Created new Community run");
                $window.location.href = ConfigService.appContext() + 'community_runs';
            }).error(function (data, status) {
                toastr.error("Unable to Community run. Please try after sometime.");
                console.log("Error " + data);
                console.log("Status " + status)
                $location.path("/");
            });
        }
    }


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
    .controller('CommunityRunCtrl', CommunityRunCtrl);
