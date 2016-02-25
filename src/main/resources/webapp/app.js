(function () {

    var gridMetadata = {
        "metricd/load/shortterm": {
            isDynamic: true,
            softLimit: 2,
            hardLimit: 20,
            index: 0
        },
        "metricd/cpu/usage": {
            isDynamic: false,
            softLimit: false,
            hardLimit: 100,
            index: 1
        },
        "metricd/memory/usage": {
            isDynamic: false,
            softLimit: false,
            hardLimit: 100,
            index: 2
        },
        "metricd/swap/usage": {
            isDynamic: false,
            softLimit: false,
            hardLimit: 100,
            index: 3
        },
        "metricd/disk/usage": {
            isDynamic: false,
            softLimit: false,
            hardLimit: 100,
            index: 4
        }
    };

    var statMetadata = {
        "metricd/network/octets/tx": {
            index: 0
        },
        "metricd/network/octets/rx": {
            index: 1
        },
        "metricd/io/ops/read": {
            index: 2
        },
        "metricd/io/ops/write": {
            index: 3
        },
        "metricd/connections/total": {
            index: 4
        }
    };

    var colorLightGreen = '#B1C6A9';
    var colorLightYellow = '#ECCA53';
    var colorLightRed = '#EF9E7A';

    var colorDarkGreen = '#6E874D';
    var colorDarkYellow = '#B28B3D';
    var colorDarkRed = '#D84C4F';

    var colorDarkGray = '#999';
    var colorLightGray = '#CCC';

    var stateOk = 'OK';
    var stateWarning = 'WARNING';

    var gridContainerWidth = 1200;
    var gridContainerHeight = 320;
    var gridTransitionDuration = 750;
    var gridBaselineWidth = 1000;
    var gridValueMultiplier = gridBaselineWidth / 100;

    var gridBaselineX = 180;
    var gridBaselineHeight = 40;
    var gridRectangleX = gridBaselineX + 2;
    var gridRectangleHeight = gridBaselineHeight - 4;

    var statContainerHeight = 120;
    var statMetricNameY = 30;
    var statMetricValueY = 80;

    var shortTransitionDuration = 500;

    var refreshDashboard = function () {
        d3.select('#information').style('color', colorDarkYellow);
        d3.json("metrics.json", function (error, json) {
            var gridData = [];
            var statData = [];
            var notice;

            if (error) {
                grayScaleDashboard();
                if (typeof (error.message) != "undefined") {
                    notice = 'Unable to refresh metrics: ' + error.message;
                } else {
                    notice = 'Unable to refresh metrics: Offline';
                }
                return d3.select('#information').text(notice).transition().duration(500).style('color', colorDarkRed);
            }

            for (var i = json.length - 1; i >= 0; i--) {
                var metricMeta;

                var metricKey = json[i].key;
                var metricValue = json[i].value;

                if (metricKey == 'metricd/metadata') {
                    notice = json[i].value.hostName + ", last updated at " + json[i].value.lastUpdated;
                }

                if (gridMetadata[metricKey] !== undefined) {
                    var percentage;
                    var limit;

                    metricMeta = gridMetadata[metricKey];
                    if (metricMeta.isDynamic && metricValue > 1) {
                        if (metricValue > metricMeta.softLimit) {
                            limit = metricMeta.hardLimit;
                        } else {
                            limit = metricMeta.softLimit;
                        }

                        percentage = (metricValue / limit) * 100;
                    } else {
                        limit = metricMeta.hardLimit;
                        percentage = (metricValue / metricMeta.hardLimit) * 100;
                    }

                    gridData.push(
                        {
                            name: json[i].name,
                            key: metricKey,
                            value: metricValue,
                            state: json[i].state,
                            description: json[i].description,
                            limit: limit,
                            percentage: percentage,
                            index: metricMeta.index
                        }
                    )
                }

                if (statMetadata[metricKey] !== undefined) {
                    metricMeta = statMetadata[metricKey];
                    statData.push(
                        {
                            name: json[i].name,
                            key: metricKey,
                            value: metricValue,
                            state: json[i].state,
                            description: json[i].description,
                            index: metricMeta.index
                        }
                    )
                }

            }

            refreshGrid(gridData);
            refreshStats(statData);

            d3.select('#information').text(notice).transition().duration(shortTransitionDuration).style('color', colorDarkGreen);
        });

    };

    var gridMetricName = function (d) {
        return d.name;
    };

    var gridMetricValue = function (d) {
        return d.value.toFixed(2);
    };

    var gridMetricLimitValue = function (d) {
        return d.limit;
    };

    var gridRectangleWidth = function (d) {
        return d.percentage * gridValueMultiplier;
    };

    var gridRectangleFillColor = function (d) {
        if (d.state == stateOk) {
            return colorDarkGreen;
        } else if (d.state == stateWarning) {
            return colorDarkYellow;
        } else {
            return colorDarkRed;
        }
    };

    var gridBaseLineFillColor = function (d) {
        if (d.state == stateOk) {
            return colorLightGreen;
        } else if (d.state == stateWarning) {
            return colorLightYellow;
        } else {
            return colorLightRed;
        }
    };

    var gridRectangleY = function (d) {
        return d.index * 60 + 22;
    };

    var gridBaselineY = function (d) {
        return d.index * 60 + 20;
    };

    var gridContainer = d3.select("#grids").append("svg")
        .attr("width", gridContainerWidth)
        .attr("height", gridContainerHeight);

    var statContainer = d3.select("#stats").append("svg")
        .attr("width", gridContainerWidth)
        .attr("height", statContainerHeight);

    var refreshGrid = function (data) {
        // Data join
        var percentages = gridContainer.selectAll(".grid-metric-percentage").data(data, gridMetricName);
        var percentageBaselines = gridContainer.selectAll(".grid-metric-percentage-baseline").data(data, gridMetricName);
        var metricNames = gridContainer.selectAll(".grid-metric-name").data(data, gridMetricName);
        var metricValues = gridContainer.selectAll(".grid-metric-value").data(data, gridMetricName);
        var metricLimitValues = gridContainer.selectAll(".grid-metric-limit-value").data(data, gridMetricName);

        // Update
        percentages.transition()
            .duration(gridTransitionDuration)
            .attr("width", gridRectangleWidth)
            .style("fill", gridRectangleFillColor);

        percentageBaselines.transition()
            .duration(gridTransitionDuration)
            .style("fill", gridBaseLineFillColor);

        metricNames.text(gridMetricName);

        metricValues.text(gridMetricValue)
            .transition()
            .duration(gridTransitionDuration)
            .style("fill", gridRectangleFillColor);

        metricLimitValues.text(gridMetricLimitValue)
            .transition()
            .duration(gridTransitionDuration)
            .style("fill", gridRectangleFillColor);

        // Enter
        percentageBaselines.enter()
            .append("rect")
            .attr("class", "grid-metric-percentage-baseline")
            .attr("y", gridBaselineY)
            .attr("x", gridBaselineX)
            .attr("height", gridBaselineHeight)
            .attr("width", 0)
            .style("fill", gridBaseLineFillColor)
            .transition()
            .duration(gridTransitionDuration)
            .attr("width", gridBaselineWidth);

        percentages.enter()
            .append("rect")
            .attr("class", "grid-metric-percentage")
            .attr("y", gridRectangleY)
            .attr("x", gridRectangleX)
            .attr("width", 0)
            .attr("height", gridRectangleHeight)
            .style("fill", gridRectangleFillColor)
            .transition()
            .duration(gridTransitionDuration)
            .attr("width", gridRectangleWidth);

        metricNames.enter()
            .append("text")
            .attr("class", "grid-metric-name")
            .attr("x", gridRectangleX - 30)
            .attr("y", gridRectangleY)
            .attr("dx", gridRectangleHeight / 2)
            .attr("dy", "10")
            .text(gridMetricName);

        metricValues.enter()
            .append("text")
            .attr("class", "grid-metric-value")
            .attr("x", gridRectangleX - 30)
            .attr("y", gridRectangleY)
            .attr("dx", gridRectangleHeight / 2)
            .attr("dy", "32")
            .style("fill", gridRectangleFillColor)
            .text(gridMetricValue);

        metricLimitValues.enter()
            .append("text")
            .attr("class", "grid-metric-limit-value")
            .attr("x", 1150)
            .attr("y", gridRectangleY)
            .attr("dx", gridRectangleHeight / 2)
            .attr("dy", "25")
            .style("fill", gridRectangleFillColor)
            .text(gridMetricLimitValue);

        // Remove
        percentageBaselines.exit().remove();
        percentages.exit().remove();
        metricNames.exit().remove();
        metricValues.exit().remove();
        metricLimitValues.exit().remove();
    };


    var statX = function (d) {
        return d.index * 200 + 180;
    };

    var statMetricValue = function (d) {
        var prefix = d3.formatPrefix(d.value);
        return prefix.scale(d.value) + ' ' + prefix.symbol;
    };

    var refreshStats = function (data) {
        var metricNames = statContainer.selectAll(".stat-metric-name").data(data, gridMetricName);
        var metricValues = statContainer.selectAll(".stat-metric-value").data(data, gridMetricName);

        metricNames.text(gridMetricName);

        metricValues.text(statMetricValue)
            .style("fill", gridRectangleFillColor);

        metricNames.enter()
            .append("text")
            .attr("class", "stat-metric-name")
            .attr("x", statX)
            .attr("y", statMetricNameY)
            .text(gridMetricName);

        metricValues.enter()
            .append("text")
            .attr("class", "stat-metric-value")
            .attr("x", statX)
            .attr("y", statMetricValueY)
            .style("fill", gridRectangleFillColor)
            .text(statMetricValue);

        metricNames.exit().remove();
        metricValues.exit().remove();
    };

    var grayScaleDashboard = function () {
        var darkerGridElements = gridContainer.selectAll(".grid-metric-percentage, .grid-metric-value, .grid-metric-limit-value");
        var lighterGridElements = gridContainer.selectAll(".grid-metric-percentage-baseline");
        var darkerStatElements = statContainer.selectAll(".stat-metric-value");

        darkerGridElements.transition()
            .duration(gridTransitionDuration)
            .style("fill", colorDarkGray);

        darkerStatElements.transition()
            .duration(gridTransitionDuration)
            .style("fill", colorDarkGray);

        lighterGridElements.transition()
            .duration(gridTransitionDuration)
            .style("fill", colorLightGray);
    };

    setInterval(function () {
        refreshDashboard();
    }, 5000);

    refreshDashboard();
})();
