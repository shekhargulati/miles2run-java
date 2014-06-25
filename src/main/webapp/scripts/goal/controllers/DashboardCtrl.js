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
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch your progress. Please try later.");
            $scope.error = {message: "Unable to fetch your progress. Please try later."};
            console.log(data);
            console.log(status);
        });

        function calendarStartDate() {
            var d = new Date();
            d.setMonth(d.getMonth() - 2);
            return d;
        }

        $scope.calendarConfig = {
            minDate: calendarStartDate(),
            maxDate: new Date(),
            data: ConfigService.getBaseUrl() + "goals/" + activeGoal.id + "/activities/calendar",
            itemName: [activeGoal.goalUnit.$name.toLowerCase(), activeGoal.goalUnit.$name.toLowerCase()]
        };


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
                        yLabelFormat: function (value) {
                            return $filter('number')(value, 2);
                        },
                        xkey: interval,
                        ykeys: ['distance', 'pace'],
                        dateFormat: function (x) {
                            if (interval === "month") {
                                return moment(dateFormat(x), "YYYYMMDD").format("MMM YYYY");
                            } else if (interval === "year") {
                                return new Date(x).getFullYear().toString();
                            }
                            return moment(dateFormat(x), "YYYYMMDD").format('dddd, MMM Do YYYY');
                        },
                        labels: ['Distance (in ' + activeGoal.goalUnit.$name.toLowerCase() + ')', 'Pace ' + '(in mins/' + activeGoal.goalUnit.$name.toLowerCase() + ')']

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

        var dateFormat = function (x) {
            var date = new Date(x);
            var yyyy = date.getFullYear().toString();
            var mm = (date.getMonth() + 1).toString();
            var dd = date.getDate().toString();
            return yyyy + "-" + (mm[1] ? mm : "0" + mm[0]) + "-" + (dd[1] ? dd : "0" + dd[0]);
        }

        function paceFormatter(v, axis) {
            return v.toFixed(axis.tickDecimals) + 'mins/' + activeGoal.goalUnit.$name.toLowerCase();
        }

        function distanceFormatter(v, axis) {
            return v.toFixed(axis.tickDecimals) + activeGoal.goalUnit.$name.toLowerCase();
        }

        function dateFormatter(val, axis) {
            var d = new Date(val);
            return d.getUTCDate() + "/" + (d.getUTCMonth() + 1);
        }


        function doPlot(position) {
            $http.get(ConfigService.getBaseUrl() + "goals/" + activeGoal.id + "/dashboard/charts/distanceandpace").success(function (data, status, headers, config) {
                var data1 = data[0];
                var data2 = data[1];

                var dataset = [
                    {
                        label: "Distance (" + activeGoal.goalUnit.$name.toLowerCase() + ")",
                        data: data1,
                        color: "#FF0000",
                        points: { fillColor: "#FF0000", show: true },
                        lines: { show: true }
                    },
                    {
                        label: "Pace (mins/" + activeGoal.goalUnit.$name.toLowerCase() + ")",
                        data: data2,
                        yaxis: 2,
                        color: "#0062E3",
                        points: { fillColor: "#0062E3", show: true },
                        lines: { show: true }
                    }
                ];

                $.plot($("#line-chart"), dataset, {
                    axisLabels: {
                        show: true
                    },
                    xaxes: [
                        {
                            mode: "time",
                            timeformat: "%m/%d",
                            minTickSize: [1, "day"],
                            color: "black",
                            axisLabel: "Activity Date",
                            tickLength: 0
                        }
                    ],
                    yaxes: [
                        {
                            min: 0,
                            tickFormatter: distanceFormatter,
                            axisLabel: "Distance (" + activeGoal.goalUnit.$name.toLowerCase() + ")",
                            position: "left",
                            tickLength: 0,
                            minTickSize: 1
                        },
                        {
                            min: 0,
                            alignTicksWithAxis: position == "right" ? 1 : null,
                            position: position,
                            tickFormatter: paceFormatter,
                            axisLabel: "Pace (mins/" + activeGoal.goalUnit.$name.toLowerCase() + ")",
                            tickLength: 0,
                            minTickSize: 1
                        }
                    ],
                    legend: {
                        position: 'nw'
                    },
                    grid: {
                        hoverable: true,
                        borderWidth: 1
                    },
                    tooltip: true,
                    tooltipOpts: {
                        content: "%s on %x was %y",
                        xDateFormat: "%m/%d/%Y",

                        onHover: function (flotItem, $tooltipEl) {
                            // console.log(flotItem, $tooltipEl);
                        }
                    }
                });
            }).error(function (data, status, headers, config) {
                console.log(data);
            });

        }

        doPlot("right");

        var renderRickshawChart = function () {
            var palette = new Rickshaw.Color.Palette();

            var dataset = [
                [
                    {
                        x: 1403116200, y: 6
                    },
                    {
                        x: 1403375400,
                        y: 2
                    },
                    {
                        x: 1403548200,
                        y: 4
                    }
                ],
                [
                    {
                        x: 1403116200, y: 9.0
                    },
                    {
                        x: 1403375400,
                        y: 12.5
                    },
                    {
                        x: 1403548200,
                        y: 16.25
                    }
                ]
            ]
            var graph = new Rickshaw.Graph({
                element: document.querySelector("#chart"),
                width: 540,
                height: 240,
                renderer: 'line',
                series: [
                    {
                        name: "Distance (mi)",
                        data: dataset[0],
                        color: "#3182bd"
                    },
                    {
                        name: "Pace (mins/mi)",
                        data: dataset[1],
                        color: "#f03b20"
                    }
                ]
            });

            var x_axis = new Rickshaw.Graph.Axis.Time({
                graph: graph
            });

            var legend = new Rickshaw.Graph.Legend({
                element: document.querySelector('#legend'),
                graph: graph
            });

            var shelving = new Rickshaw.Graph.Behavior.Series.Toggle({
                graph: graph,
                legend: legend
            });

            var highlighter = new Rickshaw.Graph.Behavior.Series.Highlight({
                graph: graph,
                legend: legend
            });

            var hoverDetail = new Rickshaw.Graph.HoverDetail({
                graph: graph,
                xFormatter: function (x) {
                    return new Date(x * 1000).toDateString();
                },
                yFormatter: function (y) {
                    return y;
                }
            });

            graph.render();
        }

        renderRickshawChart();

    });