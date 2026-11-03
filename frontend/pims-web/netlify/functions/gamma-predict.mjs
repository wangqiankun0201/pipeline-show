/**
 * Netlify 演示函数：静态站点上的 Gamma 腐蚀穿透概率。
 * 与后端 pims-risk 使用同一模型，仅用于没有后端时的公开演示。
 */
const DAYS_PER_YEAR = 365.2422

const SEGMENTS = {
  5: {
    segmentId: 5, segmentName: '峰汝线A2段', pipelineName: '峰汝线',
    commissionDate: '1990-10-01', wallThicknessMm: 11, inspectionDate: '2023-03-14',
    inspectionDepthMm: 9.35, kValue: 0.22517, n: 1.07087,
    history: [
      { date: '2011-12-29', measuredDepthMm: 5.94 },
      { date: '2023-03-14', measuredDepthMm: 9.35 }
    ]
  },
  15: {
    segmentId: 15, segmentName: '万卧线B段', pipelineName: '万卧线',
    commissionDate: '1989-06-01', wallThicknessMm: 14, inspectionDate: '2015-10-13',
    inspectionDepthMm: 4.76, kValue: 0.01697, n: 1.72263,
    history: [
      { date: '2008-10-16', measuredDepthMm: 2.8 },
      { date: '2015-10-13', measuredDepthMm: 4.76 }
    ]
  },
  16: {
    segmentId: 16, segmentName: '万卧线C段', pipelineName: '万卧线',
    commissionDate: '1989-06-01', wallThicknessMm: 14, inspectionDate: '2017-08-31',
    inspectionDepthMm: 4.06, kValue: 0.05313, n: 1.29786,
    history: [
      { date: '2009-10-23', measuredDepthMm: 2.66 },
      { date: '2017-08-31', measuredDepthMm: 4.06 }
    ]
  }
}

const days = (a, b) => (Date.parse(b + 'T00:00:00Z') - Date.parse(a + 'T00:00:00Z')) / 86400000

/* 正则化不完全 Gamma 上尾 Q(a,x)，与 Apache commons Gamma.regularizedGammaQ 一致 */
function logGamma(x) {
  const c = [76.18009172947146, -86.50532032941677, 24.01409824083091,
    -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5]
  let y = x
  let tmp = x + 5.5
  tmp -= (x + 0.5) * Math.log(tmp)
  let ser = 1.000000000190015
  for (let j = 0; j < 6; j++) ser += c[j] / ++y
  return -tmp + Math.log(2.5066282746310005 * ser / x)
}

function gammaQ(a, x) {
  if (x <= 0) return 1
  if (x < a + 1) {
    let ap = a
    let sum = 1 / a
    let del = sum
    for (let i = 0; i < 100000; i++) {
      ap += 1
      del *= x / ap
      sum += del
      if (Math.abs(del) < Math.abs(sum) * 1e-15) break
    }
    return 1 - sum * Math.exp(-x + a * Math.log(x) - logGamma(a))
  }
  const tiny = 1e-300
  let b = x + 1 - a
  let c = 1 / tiny
  let d = 1 / b
  let h = d
  for (let i = 1; i <= 100000; i++) {
    const an = -i * (i - a)
    b += 2
    d = an * d + b
    if (Math.abs(d) < tiny) d = tiny
    c = b + an / c
    if (Math.abs(c) < tiny) c = tiny
    d = 1 / d
    const del = d * c
    h *= del
    if (Math.abs(del - 1) < 1e-15) break
  }
  return Math.exp(-x + a * Math.log(x) - logGamma(a)) * h
}

function mulberry32(seed) {
  let a = seed >>> 0
  return () => {
    a = (a + 0x6d2b79f5) >>> 0
    let t = a
    t = Math.imul(t ^ (t >>> 15), t | 1)
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61)
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

function gaussian(rng) {
  const u = Math.max(rng(), 1e-12)
  return Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * rng())
}

/* Marsaglia-Tsang 方法，返回尺度为 1 的 Gamma 样本 */
function gammaSample(shape, rng) {
  if (shape < 1) return gammaSample(shape + 1, rng) * Math.pow(Math.max(rng(), 1e-12), 1 / shape)
  const d = shape - 1 / 3
  const c = 1 / Math.sqrt(9 * d)
  for (;;) {
    let x
    let v
    do {
      x = gaussian(rng)
      v = 1 + c * x
    } while (v <= 0)
    v = v * v * v
    const u = rng()
    if (u < 1 - 0.0331 * x * x * x * x) return d * v
    if (Math.log(Math.max(u, 1e-12)) < 0.5 * x * x + d * (1 - v + Math.log(v))) return d * v
  }
}

function wilson(failures, count) {
  const p = failures / count
  if (p === 0) return [0, Math.min(1, 1 - Math.pow(0.05, 1 / count))]
  if (p === 1) return [Math.max(0, Math.pow(0.05, 1 / count)), 1]
  const z = 1.959963984540054
  const z2 = z * z
  const denominator = 1 + z2 / count
  const center = (p + z2 / (2 * count)) / denominator
  const radius = z * Math.sqrt(p * (1 - p) / count + z2 / (4 * count * count)) / denominator
  return [Math.max(0, center - radius), Math.min(1, center + radius)]
}

function predict(segment, theta, sampleCount, seed) {
  const ti = days(segment.commissionDate, segment.inspectionDate) / DAYS_PER_YEAR
  const remaining = segment.wallThicknessMm - segment.inspectionDepthMm
  const years = []
  for (let year = 2026; year <= 2031; year++) years.push(year)
  const annual = years.map(year => {
    const date = `${year}-12-31`
    const t = days(segment.commissionDate, date) / DAYS_PER_YEAR
    const shape = segment.kValue * (Math.pow(t, segment.n) - Math.pow(ti, segment.n)) / theta
    const mean = segment.inspectionDepthMm + shape * theta
    return {
      year, date, tYears: t, shape, meanDepthMm: mean,
      cappedMeanDepthMm: Math.min(segment.wallThicknessMm, mean),
      remainingWallMm: Math.max(0, segment.wallThicknessMm - mean),
      probability: shape === 0 ? 0 : gammaQ(shape, remaining / theta),
      monteCarloProbability: 0, failedSamples: 0, standardError: 0,
      confidenceLow: 0, confidenceHigh: 0, zeroFailureUpper95: null
    }
  })

  // 逐年独立增量，N/2 与 N 共用同一批路径前缀
  const rng = mulberry32(seed)
  const half = Math.floor(sampleCount / 2)
  const failures = annual.map(() => 0)
  const prefixFailures = annual.map(() => 0)
  let previousShape = 0
  const increments = annual.map(row => {
    const delta = row.shape - previousShape
    previousShape = row.shape
    return delta > 0 ? delta : 0
  })
  for (let i = 0; i < sampleCount; i++) {
    let growth = 0
    for (let j = 0; j < annual.length; j++) {
      if (increments[j] > 0) growth += gammaSample(increments[j], rng) * theta
      if (remaining - growth <= 0) {
        failures[j]++
        if (i < half) prefixFailures[j]++
      }
    }
  }

  const warnings = []
  const convergence = []
  annual.forEach((row, j) => {
    const p = failures[j] / sampleCount
    row.failedSamples = failures[j]
    row.monteCarloProbability = p
    row.standardError = Math.sqrt(p * (1 - p) / sampleCount)
    const [low, high] = wilson(failures[j], sampleCount)
    row.confidenceLow = low
    row.confidenceHigh = high
    if (failures[j] === 0) row.zeroFailureUpper95 = -Math.expm1(Math.log(0.05) / sampleCount)
    convergence.push({ sampleCount: half, year: row.year, probability: prefixFailures[j] / half, standardError: Math.sqrt((prefixFailures[j] / half) * (1 - prefixFailures[j] / half) / half) })
    convergence.push({ sampleCount, year: row.year, probability: p, standardError: row.standardError })
  })
  if (failures.some(f => f === 0)) warnings.push('部分年份的模拟未观测到穿透样本，模拟概率 0 不代表解析概率为 0；请参考单侧 95% 上限。')
  if (annual.some(row => row.meanDepthMm >= segment.wallThicknessMm)) warnings.push('均值深度达到壁厚后，穿透概率仍继续计算至2031年。')
  warnings.push('当前为 Netlify 静态演示结果，与本地后端使用同一模型，但不连接真实业务数据。')

  return {
    segmentId: segment.segmentId, segmentName: segment.segmentName,
    modelVersion: 'CONDITIONAL_GAMMA_V1_DEMO',
    commissionDate: segment.commissionDate, inspectionDate: segment.inspectionDate,
    wallThicknessMm: segment.wallThicknessMm, inspectionDepthMm: segment.inspectionDepthMm,
    kValue: segment.kValue, n: segment.n, theta, sampleCount, seed, status: 'OK',
    warnings, annual, convergence,
    history: segment.history.map(h => ({
      ...h,
      calculatedDepthMm: segment.kValue * Math.pow(days(segment.commissionDate, h.date) / DAYS_PER_YEAR, segment.n)
    }))
  }
}

export default async request => {
  if (request.method !== 'POST') return json(405, { code: 'error', msg: '仅支持 POST', data: null })
  let body
  try {
    body = await request.json()
  } catch {
    return json(400, { code: 'error', msg: '请求体不是有效 JSON', data: null })
  }
  const segment = SEGMENTS[Number(body.segmentId)]
  if (!segment) return json(400, { code: 'error', msg: 'Gamma模型仅支持峰汝线A2段、万卧线B段及万卧线C段', data: null })
  const theta = body.theta == null ? 0.2 : Number(body.theta)
  const sampleCount = body.sampleCount == null ? 100000 : Number(body.sampleCount)
  const seed = body.seed == null ? 20261029 : Number(body.seed)
  if (!Number.isFinite(theta) || theta < 0.001 || theta > 5) return json(400, { code: 'error', msg: 'theta须在0.001至5之间', data: null })
  if (!Number.isInteger(sampleCount) || sampleCount < 1000 || sampleCount > 200000) return json(400, { code: 'error', msg: 'sampleCount须在1000至200000之间', data: null })
  if (!Number.isInteger(seed) || seed < 0 || seed > 2147483647) return json(400, { code: 'error', msg: 'seed须为0至2147483647之间的整数', data: null })
  return json(200, { code: 'success', msg: '操作成功', data: predict(segment, theta, sampleCount, seed) })
}

function json(status, payload) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { 'content-type': 'application/json; charset=utf-8', 'cache-control': 'no-store' }
  })
}
