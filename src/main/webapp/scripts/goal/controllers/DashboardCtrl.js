'use strict';

angular.module('milestogo')
    .controller('DashboardCtrl', function ($scope, ProgressService, ConfigService, activeProfile, $http, $timeout, $filter, activeGoal) {
        $scope.currentUser = activeProfile;
        $scope.error = null;
        $scope.data = {};

        ProgressService.progress(activeGoal.id).success(function (data, status, headers, config) {
            $scope.error = null;
            $scope.status = status;
            $scope.data = data;
            $scope.style = "width:" + data.percentage + "%";
        }).
            error(function (data, status, headers, config) {
                toastr.error("Unable to fetch your progress. Please try later.");
                $scope.error = {message: "Unable to fetch your progress. Please try later."};
                console.log(data);
                console.log(status);
            });

        var renderDistanceChart = function (interval) {
            $("#activities-line-graph").empty();
            console.log('User selected interval ' + interval);
            $http.get(ConfigService.getBaseUrl() + "goals/" + activeGoal.id + "/dashboard/charts/distance?interval=" + interval).success(function (data, status, headers, config) {
                if (data && data.length) {
                    console.log("Rendering activity distance chart as data exists");
                    $scope.showNoDistanceChartDataMessage = false;
                    Morris.Line({
                        element: 'activities-line-graph',
                        data: data,
                        xLabels: interval,
                        xLabelFormat: function (date) {
                            if (interval === "month") {
                                return moment(dateFormat(date.getTime()), "YYYYMMDD").format("MMM YYYY");
                            } else if (interval === "year") {
                                return date.getFullYear().toString();
                            }
                            return moment(dateFormat(date.getTime()), "YYYYMMDD").format('MMM Do');
                        },
                        yLabelFormat: function (distance) {
                            return distance.toString() + ' ' + activeGoal.goalUnit.$name;
                        },
                        xkey: interval,
                        ykeys: ['distance'],
                        dateFormat: function (x) {
                            if (interval === "month") {
                                return moment(dateFormat(x), "YYYYMMDD").format("MMM YYYY");
                            } else if (interval === "year") {
                                return new Date(x).getFullYear().toString();
                            }
                            return moment(dateFormat(x), "YYYYMMDD").format('dddd, MMM Do YYYY');
                        },
                        labels: ['Distance Ran']

                    });
                } else {
                    $scope.showNoDistanceChartDataMessage = true;
                }
            }).error(function (data, status, headers, config) {
                console.log(data);
                $scope.showNoDistanceChartDataMessage = false;
            });
        }

        renderDistanceChart("day");
        $scope.renderDistanceChart = renderDistanceChart;

        var renderPaceChart = function (interval) {
            $("#pace-line-graph").empty();
            console.log('User selected interval ' + interval);
            $http.get(ConfigService.getBaseUrl() + "goals/" + activeGoal.id + "/dashboard/charts/pace?interval=" + interval).success(function (data, status, headers, config) {
                if (data && data.length) {
                    console.log("Rendering pace chart as data exists");
                    $scope.showNoPaceChartDataMessage = false;
                    Morris.Line({
                        element: 'pace-line-graph',
                        data: data,
                        xLabels: interval,
                        xLabelFormat: function (date) {
                            if (interval === "month") {
                                return moment(dateFormat(date.getTime()), "YYYYMMDD").format("MMM YYYY");
                            } else if (interval === "year") {
                                return date.getFullYear().toString();
                            }
                            return moment(dateFormat(date.getTime()), "YYYYMMDD").format('MMM Do');
                        },
                        yLabelFormat: function (pace) {
                            return $filter('number')(pace, 2) + ' mins/' + activeGoal.goalUnit.$name;
                        },
                        xkey: interval,
                        ykeys: ['pace'],
                        dateFormat: function (x) {
                            if (interval === "month") {
                                return moment(dateFormat(x), "YYYYMMDD").format("MMM YYYY");
                            } else if (interval === "year") {
                                return new Date(x).getFullYear().toString();
                            }
                            return moment(dateFormat(x), "YYYYMMDD").format('dddd, MMM Do YYYY');
                        },
                        labels: ['Pace']

                    });
                } else {
                    $scope.showNoPaceChartDataMessage = true;
                }
            }).error(function (data, status, headers, config) {
                console.log(data);
                $scope.showNoPaceChartDataMessage = false;
            });
        }

        renderPaceChart("day");
        $scope.renderPaceChart = renderPaceChart;

        var dateFormat = function (x) {
            var date = new Date(x);
            var yyyy = date.getFullYear().toString();
            var mm = (date.getMonth() + 1).toString();
            var dd = date.getDate().toString();
            return yyyy + "-" + (mm[1] ? mm : "0" + mm[0]) + "-" + (dd[1] ? dd : "0" + dd[0]);
        }

    });