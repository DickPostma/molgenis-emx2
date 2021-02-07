module.exports = {
  publicPath: "", // to ensure relative paths are used
  devServer: {
    port: "9090",
    proxy: {
      "^/graphql": {
        target: "http://localhost:8080/fdpTest",
      },
      "/apps": { target: "http://localhost:8080" },
    },
  },
  configureWebpack: (config) => {
    config.devtool = "source-map";
  },
};
