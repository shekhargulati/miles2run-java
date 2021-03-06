'use strict';

angular.module('miles2run-profile')
    .controller('UserTimelineCtrl', function ($scope, $location, $http, TimelineService, userProfile, ConfigService) {

        $scope.userProfile = userProfile;

        var currentPage = 1;

        TimelineService.userGoalTimeline(userProfile.username, currentPage).success(function (data, status, headers, config) {
            $scope.activities = data.activities;
            $scope.currentPage = currentPage;
            $scope.totalItems = data.activityCount;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch home timeline. Please try after sometime.");
        });


        $scope.pageChanged = function () {
            console.log('Page changed to: ' + $scope.currentPage);
            TimelineService.userGoalTimeline(userProfile.username, $scope.currentPage).success(function (data, status, headers, config) {
                $scope.activities = data.activities;
                $scope.totalItems = data.activityCount;
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to fetch home timeline. Please try after sometime.");
            });
        };

        $scope.appContext = function () {
            return ConfigService.appContext();
        }

        $scope.messageToShare = function (activity) {
            return activity.fullname + ' ran ' + activity.distanceCovered + ' ' + activity.goalUnit + ' &via=miles2runorg';
        };

        $scope.redirectUri = function () {
            return ConfigService.absUrl();
        }

        $scope.facebookAppId = function () {
            if ($location.host() === "localhost") {
                return 433218286822536;
            } else if ($location.host() === "www.miles2run.org") {
                return 1466042716958015;
            }
            return 1441151639474875;
        }

        $scope.facebookShareUrl = function (activity) {
            return "https://www.facebook.com/dialog/feed?redirect_uri=" + $scope.redirectUri() + "&link=" + $scope.activityUrl(activity) + "&display=popup&description=" + $scope.messageToShare(activity) + "&app_id=" + $scope.facebookAppId();
        }

        $scope.twitterShareUrl = function (activity) {
            return "https://twitter.com/intent/tweet?url=" + $scope.activityUrl(activity) + "&text=" + $scope.messageToShare(activity);
        }

        $scope.googleShareUrl = function (activity) {
            return "https://plus.google.com/share?url=" + $scope.activityUrl(activity);
        }

        $scope.activityUrl = function (activity) {
            return ConfigService.absUrl() + 'users/' + activity.username + '/activities/' + activity.id;
        }
    });
