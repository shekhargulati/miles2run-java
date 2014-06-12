'use strict';

angular.module('miles2run-home')
    .controller('HomeTimelineCtrl', function ($scope, $http, $modal, $location, activeProfile, ConfigService) {
        $scope.currentUser = activeProfile;

        if (!angular.isDefined($scope.currentPage)) {
            $scope.currentPage = 1;
        }
        $http.get(ConfigService.getBaseUrl() + 'activities/home_timeline').success(function (data, status, headers, config) {
            $scope.activities = data.timeline;
            $scope.totalItems = data.totalItems;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch home timeline. Please try after sometime.");
        });

        $scope.pageChanged = function () {
            console.log('Page changed to: ' + $scope.currentPage);
            $http.get(ConfigService.getBaseUrl() + 'activities/home_timeline').success(function (data, status, headers, config) {
                $scope.activities = data.timeline;
                $scope.totalItems = data.totalItems;
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to fetch home timeline. Please try after sometime.");
            });
        };

        $scope.messageToShare = function (activity) {
            return activity.fullname + ' ran ' + activity.distanceCovered + ' ' + activity.goalUnit + ' &via=miles2runorg';
        };

        $scope.redirectUri = function () {
            if ($location.host() === "localhost") {
                return "http://localhost:8080/miles2run"
            }
            return "http://" + $location.host();
        }

        $scope.facebookAppId = function () {
            if ($location.host() === "localhost") {
                return 433218286822536;
            }
            return 433218286822536;
        }

        $scope.delete = function (idx) {
            var modalIntance = $modal.open({
                templateUrl: "confirm.html",
                controller: DeleteActivityCtrl,
                resolve: {
                    activityToDelete: function () {
                        var activityToDelete = $scope.activities[idx];
                        return activityToDelete;
                    },
                    idx: function () {
                        return idx;
                    },
                    activities: function () {
                        return $scope.activities;
                    }
                }
            })

        };

    });

var DeleteActivityCtrl = function ($scope, $modalInstance, activityToDelete, idx, activities, ConfigService, $http) {

    $scope.ok = function () {
        $http.delete(ConfigService.getBaseUrl() + "goals/" + activityToDelete.goalId + "/activities/" + activityToDelete.id).success(function (data, status) {
            toastr.success("Deleted activity");
            activities.splice(idx, 1);
            $modalInstance.close({});
        }).error(function (data, status, headers, config) {
            console.log("Status code %s", status);
            if (status == 401) {
                toastr.error("You are not authorized to perform this operation.")
            } else {
                toastr.error("Unable to delete activity. Please try later.");
            }
            $modalInstance.close({});
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};