module.exports = {
  devServer: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        pathRewrite: {}
      }
    }
  },
  chainWebpack: config => {
    config.plugins.delete('html')
    config.plugins.delete('preload')
    config.plugins.delete('prefetch')
    config.plugins.delete('copy')
    config.plugin('html').use(require('html-webpack-plugin'), [{
      template: './public/index.html',
      inject: true,
      filename: 'index.html'
    }])
  }
}